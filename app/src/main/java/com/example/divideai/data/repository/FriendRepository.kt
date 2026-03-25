package com.example.divideai.data.repository

import com.example.divideai.data.model.FriendRequest
import com.example.divideai.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio responsavel por gerenciar as operacoes de dados relacionadas a amizades.
 * Interage com o Firebase Firestore para enviar solicitacoes, listar amigos, buscar usuarios e gerenciar status.
 */
class FriendRepository {
    private val db = FirebaseFirestore.getInstance()
    private val friendsCollection = db.collection("friends")

    /**
     * Envia uma nova solicitacao de amizade caso ela (ou a amizade em si) ainda nao exista.
     *
     * @param sender O usuario que esta enviando a solicitacao.
     * @param receiver O usuario que ira receber a solicitacao.
     * @return Um [Result] indicando sucesso ou detalhando a falha na operacao.
     */
    suspend fun sendFriendRequest(sender: User, receiver: User): Result<Boolean> {
        return try {
            // Check if request already exists
            val existingRequest = friendsCollection
                .whereEqualTo("senderId", sender.id)
                .whereEqualTo("receiverId", receiver.id)
                .get()
                .await()
            
            val existingReverse = friendsCollection
                .whereEqualTo("senderId", receiver.id)
                .whereEqualTo("receiverId", sender.id)
                .get()
                .await()

            if (!existingRequest.isEmpty || !existingReverse.isEmpty) {
                return Result.failure(Exception("Já existe uma solicitação ou amizade com este usuário."))
            }

            val docRef = friendsCollection.document()
            val request = FriendRequest(
                id = docRef.id,
                senderId = sender.id,
                senderName = sender.name,
                senderEmail = sender.email,
                receiverId = receiver.id,
                receiverName = receiver.name,
                receiverEmail = receiver.email,
                status = "pending"
            )

            docRef.set(request).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtem a lista de todas as amizades ativas (com status "accepted") associadas a um determinado usuario.
     *
     * @param userId O ID do usuario para o qual os amigos serao buscados.
     * @return Uma lista de objetos [FriendRequest] representando as amizades confirmadas.
     */
    suspend fun getFriends(userId: String): List<FriendRequest> {
        return try {
            // Firestore doesn't support logical OR queries natively on the root level easily without multiple queries
            val sentAccepted = friendsCollection
                .whereEqualTo("senderId", userId)
                .whereEqualTo("status", "accepted")
                .get()
                .await()
                .toObjects(FriendRequest::class.java)

            val receivedAccepted = friendsCollection
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("status", "accepted")
                .get()
                .await()
                .toObjects(FriendRequest::class.java)

            sentAccepted + receivedAccepted
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtem as solicitacoes de amizade que foram recebidas pelo usuario e ainda estao pendentes (status "pending").
     *
     * @param userId O ID do usuario destinatario das solicitacoes.
     * @return Uma lista de objetos [FriendRequest] recebidos que aguardam resposta.
     */
    suspend fun getPendingRequests(userId: String): List<FriendRequest> {
        return try {
            friendsCollection
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("status", "pending")
                .get()
                .await()
                .toObjects(FriendRequest::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Responde a uma solicitacao de amizade pendente, seja aceitando-a ou recusando-a.
     *
     * @param requestId O identificador (ID) do documento da solicitacao de amizade.
     * @param accept Verdadeiro (true) para aceitar (status muda para "accepted"), falso (false) para recusar (o documento e deletado).
     * @return Um [Result] indicando o sucesso ou a falha da operacao de atualizacao/remocao.
     */
    suspend fun respondToRequest(requestId: String, accept: Boolean): Result<Boolean> {
        return try {
            val docRef = friendsCollection.document(requestId)
            if (accept) {
                docRef.update("status", "accepted").await()
            } else {
                docRef.delete().await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Realiza uma pesquisa simples por usuarios cujo nome ou email contenham o texto fornecido (case-insensitive).
     * Exclui o proprio usuario atual dos resultados da busca.
     *
     * @param query O termo utilizado para a pesquisa.
     * @param currentUserId O ID do usuario atual para filtragem.
     * @return Uma lista de usuarios ([User]) correspondentes ao criterio de busca.
     */
    suspend fun searchUsers(query: String, currentUserId: String): List<User> {
         return try {
            // Very simple text search. Since Firestore doesn't support 'LIKE', we often use Range Queries. 
            // In a real app we would use Algolia or Firebase Extensions. 
            // For now, let's just fetch all and filter client side (OK for a small sample, but bad for prod)
             val result = db.collection("users").get().await()
             result.toObjects(User::class.java).filter {
                 it.id != currentUserId && 
                 (it.name.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true))
             }
         } catch (e: Exception) {
             emptyList()
         }
    }
    /**
     * Remove uma amizade existente ou solicitacao entre dois usuarios, excluindo do banco os documentos correspondentes.
     * Considera qualquer direcao de relacionamento (user1 -> user2 ou user2 -> user1).
     *
     * @param userId1 O ID de um dos amigos.
     * @param userId2 O ID do outro amigo.
     * @return Um [Result] de sucesso, indicando se as instancias da amizade foram removidas com exito.
     */
    suspend fun removeFriendship(userId1: String, userId2: String): Result<Boolean> {
        return try {
            val query1 = friendsCollection
                .whereEqualTo("senderId", userId1)
                .whereEqualTo("receiverId", userId2)
                .get()
                .await()

            val query2 = friendsCollection
                .whereEqualTo("senderId", userId2)
                .whereEqualTo("receiverId", userId1)
                .get()
                .await()
            
            for (doc in query1.documents) {
                friendsCollection.document(doc.id).delete().await()
            }
            for (doc in query2.documents) {
                friendsCollection.document(doc.id).delete().await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
