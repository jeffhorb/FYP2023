// Import the Firebase SDK
const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

// Define the function that triggers on Firestore onCreate event
exports.sendNotificationOnNewTask = functions.firestore
    .document("userTasks/{userTaskId}") // Specify the path
    .onCreate(async (snap, context) => {
    // Get the new document data
      const newTask = snap.data();

      // Get the user id and task id from the new document
      const { userId } = newTask;
      const { taskId } = newTask;

      // Get the user name and FCM token from the Users collection
      const userRef = admin.firestore().collection("Users").doc(userId);
      const userSnap = await userRef.get();
      const { userName } = userSnap.data();
      const { fcmToken } = userSnap.data();

      // Get the task name from the Tasks collection
      const taskRef = admin.firestore().collection("Tasks").doc(taskId);
      const taskSnap = await taskRef.get();
      const { taskName } = taskSnap.data();

      // Define the notification payload
      const payload = {
        notification: {
          title: "You have a new task assigned!",
          body: `Hello, ${userName}! You have a new task: ${taskName}`,
        },
        data: {
          title: "You have a new task assigned!",
          body: `Hello, ${userName}! You have a new task: ${taskName}`,
        },
      };
      // Send the notification to the FCM token
      return admin.messaging().sendToDevice(fcmToken, payload);
    });

// Define the function that triggers on Firestore onDelete event
exports.sendNotificationOnTaskDeleted = functions.firestore
    .document("userTasks/{userTaskId}") // Specify the path
    .onDelete(async (snap, context) => {
    // Get the deleted document data
      const deletedTask = snap.data();

      // Get the user id and task id from the deleted document
      const { userId } = deletedTask;
      const { taskId } = deletedTask;

      // Get the user name and FCM token from the Users collection
      const userRef = admin.firestore().collection("Users").doc(userId);
      const userSnap = await userRef.get();
      const { userName } = userSnap.data();
      const { fcmToken } = userSnap.data();

      // Get the task name from the Tasks collection
      const taskRef = admin.firestore().collection("Tasks").doc(taskId);
      const taskSnap = await taskRef.get();
      const { taskName } = taskSnap.data();

      // Define the notification payload
      const payload = {
        notification: {
          title: "Task Unassigned",
          body: `Hello, ${userName}! You have been unassigned from ${taskName}`,
        },
        data: {
          title: "Task Unassigned",
          body: `Hello, ${userName}! You have been unassigned from ${taskName}`,
        },
      };
      // Send the notification to the FCM token
      return admin.messaging().sendToDevice(fcmToken, payload);
    });

// Define the function that triggers on Firestore onCreate event for Tasks
exports.sendNotificationOnNewTaskAdded = functions.firestore
    .document("Tasks/{taskId}")
    .onCreate(async (snap, context) => {
      // Get the new document data
      const newTask = snap.data();

      // Get all users from the Users collection
      const usersRef = await admin.firestore().collection("Users").get();

      // Define the notification payload for new task added
      const payload = {
        notification: {
          title: "New Task Added!",
          body: `A new task has been added: ${newTask.taskName}`,
        },
        data: {
          title: "New Task Added!",
          body: `A new task has been added: ${newTask.taskName}`,
        },
      };

      // Send the notification to each user in the Users collection
      const messagingPromises = usersRef.docs.map(async (userDoc) => {
        const { fcmToken } = userDoc.data();
        return admin.messaging().sendToDevice(fcmToken, payload);
      });

      return Promise.all(messagingPromises);
    });

// Define the function that triggers on Firestore onDelete event for Tasks
exports.sendNotificationOnTaskRemoved = functions.firestore
    .document("Tasks/{taskId}")
    .onDelete(async (snap, context) => {
      // Get the deleted document data
      const deletedTask = snap.data();

      // Get all users from the Users collection
      const usersRef = await admin.firestore().collection("Users").get();

      // Define the notification payload for task removal
      const removalPayload = {
        notification: {
          title: "Task Removed",
          body: `The task has been removed: ${deletedTask.taskName}`,
        },
        data: {
          title: "Task Removed",
          body: `The task has been removed: ${deletedTask.taskName}`,
        },
      };

      // Send the notification to each user in the Users collection
      const messagingPromises = usersRef.docs.map(async (userDoc) => {
        const { fcmToken } = userDoc.data();
        return admin.messaging().sendToDevice(fcmToken, removalPayload);
      });

      return Promise.all(messagingPromises);
    });
