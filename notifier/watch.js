/**
 * Watches Firestore for new friend requests and new expenses, and fires FCM
 * pushes to the relevant users. Run this on your machine while testing, or
 * deploy it to a small server / Cloud Run for production use.
 *
 *   cd notifier && npm install
 *   node watch.js
 *
 * The watcher needs the same `service-account.json` file as `send.js`.
 *
 * It listens to two collections:
 *   - friendRequests : on a new doc, notifies the receiver
 *   - expenses       : on a new doc, notifies every participant other than the payer
 *
 * The startup query sets a baseline so existing docs don't trigger pushes when
 * the watcher restarts.
 */

const admin = require('firebase-admin');
const path = require('path');
const fs = require('fs');

const SERVICE_ACCOUNT_PATH = path.join(__dirname, 'service-account.json');

if (!fs.existsSync(SERVICE_ACCOUNT_PATH)) {
  console.error(
    `Missing ${SERVICE_ACCOUNT_PATH}.\n` +
      'Generate one in Firebase Console > Project settings > Service accounts > Generate new private key.'
  );
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.cert(require(SERVICE_ACCOUNT_PATH)),
});

const db = admin.firestore();
const startedAt = admin.firestore.Timestamp.now();

async function sendToUser(uid, title, body) {
  const userDoc = await db.collection('users').doc(uid).get();
  const token = userDoc.get('fcmToken');
  if (!token) {
    console.warn(`[skip] user ${uid} has no fcmToken`);
    return;
  }
  try {
    await admin.messaging().send({
      token,
      notification: { title, body },
      android: { priority: 'high' },
    });
    console.log(`[sent] ${uid} <- "${title}"`);
  } catch (err) {
    console.warn(`[fail] ${uid}: ${err.message}`);
  }
}

function watchFriendRequests() {
  db.collection('friendRequests')
    .where('createdAt', '>=', startedAt)
    .onSnapshot((snap) => {
      for (const change of snap.docChanges()) {
        if (change.type !== 'added') continue;
        const data = change.doc.data();
        const senderName = data.senderName || data.senderEmail || 'Alguém';
        sendToUser(
          data.receiverId,
          'Nova solicitação de amizade',
          `${senderName} quer ser seu amigo no DivideAi.`
        );
      }
    });
  console.log('Listening on friendRequests…');
}

function watchExpenses() {
  db.collection('expenses')
    .where('createdAt', '>=', startedAt)
    .onSnapshot((snap) => {
      for (const change of snap.docChanges()) {
        if (change.type !== 'added') continue;
        const data = change.doc.data();
        const title = data.title || 'Despesa';
        const payerId = data.payerId;
        const participants = (data.participants || []).map((p) => p.userId).filter(Boolean);
        for (const uid of participants) {
          if (uid === payerId) continue;
          sendToUser(
            uid,
            'Nova despesa no grupo',
            `Você foi incluído em "${title}".`
          );
        }
      }
    });
  console.log('Listening on expenses…');
}

watchFriendRequests();
watchExpenses();
