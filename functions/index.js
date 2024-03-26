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

// Define the function that triggers on Firestore onCreate event for Tasks
exports.sendNotificationOnNewProjectAdded = functions.firestore
    .document("Projects/{projectId}")
    .onCreate(async (snap, context) => {
      // Get the new document data
      const newProject = snap.data();

      // Get all users from the Users collection
      const usersRef = await admin.firestore().collection("Users").get();

      // Define the notification payload for new task added
      const payload = {
        notification: {
          title: "New Project Added!",
          body: `A new project has been added: ${newProject.title}`,
        },
        data: {
          title: "New Project Added!",
          body: `A new project has been added: ${newProject.title}`,
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
exports.sendNotificationOnProjectRemoved = functions.firestore
    .document("Project/{projectId}")
    .onDelete(async (snap, context) => {
      // Get the deleted document data
      const deletedProject = snap.data();

      // Get all users from the Users collection
      const usersRef = await admin.firestore().collection("Users").get();

      // Define the notification payload for task removal
      const removalPayload = {
        notification: {
          title: "Project Removed",
          body: `A project has been removed: ${deletedProject.title}`,
        },
        data: {
          title: "Project Removed",
          body: `A project has been removed: ${deletedProject.title}`,
        },
      };

      // Send the notification to each user in the Users collection
      const messagingPromises = usersRef.docs.map(async (userDoc) => {
        const { fcmToken } = userDoc.data();
        return admin.messaging().sendToDevice(fcmToken, removalPayload);
      });

      return Promise.all(messagingPromises);
    });

exports.sendNotificationOnTaskCompletion = functions.firestore
    .document("Tasks/{taskId}") // Specify the path
    .onUpdate(async (change, context) => {
      // Get the updated task data
      const updatedTask = change.after.data();
      const previousTask = change.before.data();

      // Check if the progress field is updated to "Complete"
      const isTaskCompleted = updatedTask.progress === "Complete" &&
      previousTask.progress !== "Complete";

      if (isTaskCompleted) {
        // Retrieve all users from the Users collection
        const usersRef = admin.firestore().collection("Users");
        const usersSnap = await usersRef.get();

        // Get the task name
        const { taskName } = updatedTask;

        // Define the notification payload for task completion
        const taskCompletionPayload = {
          notification: {
            title: "Task Completed",
            body: `The task '${taskName}' has been completed.`,
          },
          data: {
            title: "Task Completed",
            body: `The task '${taskName}' has been completed.`,
          },
        };

        // Send the notification to each user in the Users collection
        const notifications = [];
        usersSnap.forEach((userDoc) => {
          const { fcmToken } = userDoc.data();
          notifications.push(admin.messaging()
              .sendToDevice(fcmToken, taskCompletionPayload));
        });

        return Promise.all(notifications);
      }

      return null; // No notifications not updated to "Complete"
    });

exports.sendTaskReminders = functions.pubsub.schedule("every 24 hours")
    .timeZone("UTC").onRun(async (context) => {
      const currentDate = new Date();
      const startDate = new Date(currentDate);
      startDate.setDate(startDate.getDate() + 3);

      const endDate = new Date(currentDate);
      endDate.setDate(endDate.getDate() + 1);

      const tasksRef = admin.firestore().collection("Tasks");
      const query = tasksRef.where(
          "estimatedEndDate",
          ">=",
          startDate,
      ).where("estimatedEndDate", "<=", endDate);

      const tasksSnapshot = await query.get();

      const reminderPromises = tasksSnapshot.docs.map(async (taskDoc) => {
        const taskData = taskDoc.data();
        const taskId = taskDoc.id;

        // Check task progress before sending a reminder
        if (taskData.progress !== "Complete") {
          // Fetch assigned user's information from userTasks collection
          const userTasksRef = admin.firestore().collection("userTasks");
          const userTasksQuery = userTasksRef.where("taskId", "==", taskId);
          const userTasksSnapshot = await userTasksQuery.get();

          if (!userTasksSnapshot.empty) {
            const userTaskData = userTasksSnapshot.docs[0].data();
            const assignedUserId = userTaskData.userId;

            // Get assigned user data
            const userRef = admin.firestore()
                .collection("Users").doc(assignedUserId);
            const userSnap = await userRef.get();

            if (userSnap.exists) {
              const userData = userSnap.data();
              const { fcmToken } = userData;

              // Construct the reminder notification payload
              const payload = {
                notification: {
                  title: "Task Reminder",
                  body: `Reminder: The task '${taskData.taskName}'
                       is ending on ${taskData.endDate
      .toDate().toLocaleDateString()}.`,
                },
                data: {
                  title: "Task Reminder",
                  body: `Reminder: The task '${taskData.taskName}'
                          is ending on ${taskData.endDate.toDate()
      .toLocaleDateString()}.`,
                  taskId,
                },
              };

              // Send the reminder notification
              return admin.messaging().sendToDevice(fcmToken, payload);
            }
          }
        }

        return null;
      });

      return Promise.all(reminderPromises);
    });

