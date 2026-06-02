/**
 * Sends a single FCM push notification to a DivideAi user.
 *
 * Usage:
 *   node send.js --uid <userId> --title "Title" --body "Body"
 *   node send.js --email user@example.com --title "Title" --body "Body"
 *
 * Setup:
 *   1. Generate a service account key in Firebase Console:
 *        Project settings > Service accounts > Generate new private key
 *      Save the downloaded JSON as `service-account.json` in this folder.
 *   2. Install deps:
 *        cd notifier && npm install
 *   3. Run the command above.
 *
 * The app saves each device's FCM token under
 *   users/{uid}.fcmToken
 * so this script just reads it back and calls messaging().send().
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

function parseArgs(argv) {
  const args = {};
  for (let i = 2; i < argv.length; i += 2) {
    const key = argv[i].replace(/^--/, '');
    args[key] = argv[i + 1];
  }
  return args;
}

async function resolveUid(args) {
  if (args.uid) return args.uid;
  if (args.email) {
    const snap = await db.collection('users').where('email', '==', args.email).limit(1).get();
    if (snap.empty) throw new Error(`No user found with email ${args.email}`);
    return snap.docs[0].id;
  }
  throw new Error('Pass --uid <id> or --email <email> to target a user.');
}

async function main() {
  const args = parseArgs(process.argv);
  const title = args.title || 'DivideAi';
  const body = args.body || '';

  const uid = await resolveUid(args);
  const userDoc = await db.collection('users').doc(uid).get();
  if (!userDoc.exists) throw new Error(`User ${uid} does not exist.`);

  const token = userDoc.get('fcmToken');
  if (!token) {
    throw new Error(
      `User ${uid} has no fcmToken saved yet. Make sure the app was opened at least once after this user logged in.`
    );
  }

  const response = await admin.messaging().send({
    token,
    notification: { title, body },
    android: { priority: 'high' },
  });

  console.log(`Sent message ${response} to ${uid}.`);
}

main().catch((err) => {
  console.error(err.message);
  process.exit(1);
});
