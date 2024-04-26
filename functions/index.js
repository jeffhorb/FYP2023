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
    .document("userTasks/{userTaskId}")
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

      // Get the groupId from the task document
      const { groupId } = newTask;

      // Fetch the group document based on the groupId
      const groupDoc = await admin.firestore().collection("groups")
          .doc(groupId).get();
      if (!groupDoc.exists) {
        console.log("Group document not found");
        return null;
      }

      // Get the list of member userAuthIDs from the group
      const memberUserAuthIDs = groupDoc.data().members;

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

      // Send the notification to each member of the group
      const messagingPromises = memberUserAuthIDs.map(async (userAuthID) => {
        // Get user document from Users collection based on the userAuthID
        const userQuerySnapshot = await admin.firestore().collection("Users")
            .where("userId", "==", userAuthID).get();
        if (userQuerySnapshot.empty) {
          console.log(`User document not found for userAuthID: ${userAuthID}`);
          return null;
        }

        // Get the FCM token from the user document
        const userData = userQuerySnapshot.docs[0].data();
        const { fcmToken } = userData;

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

      // Get the groupId from the task document
      const { groupId } = deletedTask;

      // Fetch the group document based on the groupId
      const groupDoc = await admin.firestore().collection("groups")
          .doc(groupId).get();
      if (!groupDoc.exists) {
        console.log("Group document not found");
        return null;
      }

      // Get the list of member userAuthIDs from the group
      const memberUserAuthIDs = groupDoc.data().members;

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

      // Send the notification to each member of the group
      const messagingPromises = memberUserAuthIDs.map(async (userAuthID) => {
        // Get user document from Users collection based on the userAuthID
        const userQuerySnapshot = await admin.firestore().collection("Users")
            .where("userId", "==", userAuthID).get();
        if (userQuerySnapshot.empty) {
          console.log(`User document not found for userAuthID: ${userAuthID}`);
          return null;
        }

        // Get the FCM token from the user document
        const userData = userQuerySnapshot.docs[0].data();
        const { fcmToken } = userData;

        return admin.messaging().sendToDevice(fcmToken, removalPayload);
      });

      return Promise.all(messagingPromises);
    });


// Define the function that triggers on Firestore onCreate event for Projects
exports.sendNotificationOnNewProjectAdded = functions.firestore
    .document("Projects/{projectId}")
    .onCreate(async (snap, context) => {
      // Get the new project document data
      const newProject = snap.data();

      // Get the groupId of the project
      const { groupId } = newProject;

      // Fetch the group document based on the groupId
      const groupDoc = await admin.firestore().collection("groups")
          .doc(groupId).get();
      if (!groupDoc.exists) {
        console.log("Group document not found");
        return null;
      }

      // Get the list of member userAuthIDs from the group document
      const { members } = groupDoc.data();

      // Define the notification payload for new project added
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

      // Send the notification to each member of the group
      const messagingPromises = members.map(async (userId) => {
        // Get the user doc from the Users collection based on the userAuthID
        const userQuerySnapshot = await admin.firestore().collection("Users")
            .where("userId", "==", userId).get();
        if (userQuerySnapshot.empty) {
          console.log(`User not found for userAuthID: ${userId}`);
          return null;
        }

        // Get the FCM token from the user document
        const userData = userQuerySnapshot.docs[0].data();
        const { fcmToken } = userData;

        return admin.messaging().sendToDevice(fcmToken, payload);
      });

      return Promise.all(messagingPromises);
    });

// Define the function that triggers on Firestore onDelete event for Projects
exports.sendNotificationOnProjectRemoved = functions.firestore
    .document("Projects/{projectId}")
    .onDelete(async (snap, context) => {
      // Get the deleted project document data
      const deletedProject = snap.data();

      // Get the groupId of the project
      const { groupId } = deletedProject;

      // Fetch the group document based on the groupId
      const groupDoc = await admin.firestore().collection("groups")
          .doc(groupId).get();
      if (!groupDoc.exists) {
        console.log("Group document not found");
        return null;
      }

      // Get the list of member userAuthIDs from the group document
      const memberUserAuthIDs = groupDoc.data().members;

      // Define the notification payload for project removal
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

      // Send the notification to each member of the group
      const messagingPromises = memberUserAuthIDs.map(async (userAuthID) => {
        // Get the user document from the Users collection based on the userID
        const userQuerySnapshot = await admin.firestore().collection("Users")
            .where("userId", "==", userAuthID).get();
        if (userQuerySnapshot.empty) {
          console.log(`User document not found for userAuthID: ${userAuthID}`);
          return null;
        }

        // Get the FCM token from the user document
        const userData = userQuerySnapshot.docs[0].data();
        const { fcmToken } = userData;

        return admin.messaging().sendToDevice(fcmToken, removalPayload);
      });

      return Promise.all(messagingPromises);
    });


exports.sendNotificationOnTaskCompletion = functions.firestore
    .document("Tasks/{taskId}")
    .onUpdate(async (change, context) => {
    // Get the updated task data
      const updatedTask = change.after.data();
      const previousTask = change.before.data();

      // Check if the progress field is updated to "Complete"
      const isTaskCompleted = updatedTask.progress === "Complete" &&
      previousTask.progress !== "Complete";

      if (isTaskCompleted) {
      // Get the groupId from the updated task document
        const { groupId } = updatedTask;

        try {
        // Retrieve the group document based on the groupId
          const groupDoc = await admin.firestore()
              .collection("groups").doc(groupId).get();

          if (groupDoc.exists) {
          // Get the list of member userAuthIDs from the group
            const memberUserAuthIDs = groupDoc.data().members;

            // Define the notification payload for task completion
            const taskCompletionPayload = {
              notification: {
                title: "Task Completed",
                body: `The task '${updatedTask.taskName}' has been completed.`,
              },
              data: {
                title: "Task Completed",
                body: `The task '${updatedTask.taskName}' has been completed.`,
              },
            };

            // Send the notification to each member of the group
            const messagingPromises = memberUserAuthIDs
                .map(async (userAuthID) => {
                  // Retrieve the user's FCM token from the Users collection
                  const userSnapshot = await admin.firestore()
                      .collection("Users")
                      .where("userId", "==", userAuthID).get();
                  if (!userSnapshot.empty) {
                    const userData = userSnapshot.docs[0].data();
                    const { fcmToken } = userData;
                    if (fcmToken) {
                      return admin.messaging()
                          .sendToDevice(fcmToken, taskCompletionPayload);
                    }
                  }
                  return null;
                });

            await Promise.all(messagingPromises);
          } else {
            console.log("Group document not found");
          }
        } catch (error) {
          console.error("Error fetching data:", error);
        }
      }

      return null; // No notifications if the task is not updated to "Complete"
    });


exports.sendGroupInvitationNotification = functions.firestore
    .document("invitations/{invitationId}")
    .onCreate((snapshot, context) => {
      const invitation = snapshot.data();
      const { userId } = invitation;
      const { groupId } = invitation;
      const { groupName } = invitation;

      // Retrieve the user's FCM token
      return admin.firestore().collection("Users").doc(userId).get()
          .then((userDoc) => {
            const userData = userDoc.data();
            const { fcmToken } = userData;

            if (fcmToken) {
              // Construct the notification payload
              const payload = {
                notification: {
                  title: "You have a new group invitation",
                  body: `You have been invited to join the group: ${groupName}`,
                  // clickAction: 'FLUTTER_NOTIFICATION_CLICK'
                },
                data: {
                  groupId,
                  click_action: "OPEN_PENDING_INVITES_ACTIVITY",
                },
              };

              // Send the notification
              return admin.messaging().sendToDevice(fcmToken, payload);
            }
            console.log("User does not have an FCM token");
            return null;
          })
          .catch((error) => {
            console.error("Error fetching user data:", error);
            return null;
          });
    });
exports.sendCommentNotification = functions.firestore
    .document("Comments/{commentId}")
    .onCreate(async (snapshot, context) => {
    // Get the comment data
      const commentData = snapshot.data();
      const { groupId, userName, currentUserId } = commentData;
      const { commentId } = context.params;

      try {
      // Retrieve the project ID associated with the comment
        const projectCommentQuerySnapshot = await admin.firestore()
            .collection("ProjectComments")
            .where("commentId", "==", commentId).get();

        if (projectCommentQuerySnapshot.empty) {
          console.log(`No project found for commentId: ${commentId}`);
          return null;
        }

        const { projectId } = projectCommentQuerySnapshot.docs[0].data();

        // Retrieve the project document based on the projectId
        const projectDoc = await admin.firestore().collection("Projects")
            .doc(projectId).get();

        if (!projectDoc.exists) {
          console.log(`No project found with projectId: ${projectId}`);
          return null;
        }

        const { title } = projectDoc.data();

        // Retrieve the group document based on the groupId
        const groupDoc = await admin.firestore().collection("groups")
            .doc(groupId).get();

        if (groupDoc.exists) {
        // Get the list of member userAuthIDs from the group
          const memberUserAuthIDs = groupDoc.data().members;

          // Define the notification payload for new comment
          const commentPayload = {
            notification: {
              title: "New Comment in Project",
              body: `${userName} commented on the project "${title}".`,
            },
            data: {
              groupId,
              click_action: "OPEN_GROUP_ACTIVITY",
            },
          };

          // Send notification to members of the group, except comment sender
          const messagingPromises = memberUserAuthIDs
              .map(async (userAuthID) => {
                if (userAuthID !== currentUserId) {
                  // Retrieve the user's FCM token from the Users collection
                  const userSnapshot = await admin
                      .firestore().collection("Users")
                      .where("userId", "==", userAuthID).get();

                  if (!userSnapshot.empty) {
                    const userData = userSnapshot.docs[0].data();
                    const { fcmToken } = userData;

                    if (fcmToken) {
                      return admin.messaging()
                          .sendToDevice(fcmToken, commentPayload);
                    }
                  }
                  return null; // null if userSnapshot is empty
                }
                return null; // null if userAuthID is comment sender
              });

          await Promise.all(messagingPromises);
        } else {
          console.log("Group document not found");
        }
      } catch (error) {
        console.error("Error fetching data:", error);
      }

      return null; // End function execution
    });


exports.sendInvitationNotification = functions.firestore
    .document("invitations/{invitationId}")
    .onDelete(async (snapshot, context) => {
      const invitation = snapshot.data();
      const { userId } = invitation;
      const { groupId } = invitation;
      const { groupName } = invitation;

      // Retrieve the user's authId from the Users collection
      const userDoc = await admin.firestore().collection("Users")
          .doc(userId).get();
      if (!userDoc.exists) {
        console.log(`No user found with userId: ${userId}`);
        return null;
      }
      const userData = userDoc.data();
      const { userId: authId } = userData; // Use authId from user document

      // Check if the user is a member of the group
      const groupDoc = await admin.firestore().collection("groups")
          .doc(groupId).get();
      const groupData = groupDoc.data();
      const { members } = groupData;

      if (members.includes(authId)) {
        // The user accepted the invitation
        // Send a notification to all group members
        const payload = {
          notification: {
            title: "New Group Member",
            body: `A new member has joined the group "${groupName}".`,
          },
          data: {
            groupId,
            click_action: "OPEN_GROUP_ACTIVITY",
          },
        };

        // Send the notification to all group members
        const messagingPromises = members.map(async (memberId) => {
          const memberDoc = await admin.firestore().collection("Users")
              .where("userId", "==", memberId).get();
          if (memberDoc.empty) {
            console.log(`No user found with authId: ${memberId}`);
            return null;
          }
          const memberData = memberDoc.docs[0].data();
          const { fcmToken } = memberData;
          if (fcmToken) {
            return admin.messaging().sendToDevice(fcmToken, payload);
          }
          return null;
        });

        await Promise.all(messagingPromises);
      } else {
        // The invitation was rescinded
        // Send a notification to the user
        const { fcmToken } = userData;

        if (fcmToken) {
          const payload = {
            notification: {
              title: "Group Invitation Rescinded",
              body: `Your invitation to join the group
              "${groupName}" has been rescinded.`,
            },
            data: {
              groupId,
              click_action: "OPEN_INVITATION_ACTIVITY",
            },
          };

          // Send the notification
          await admin.messaging().sendToDevice(fcmToken, payload);
        } else {
          console.log("User does not have an FCM token");
        }
      }
      return null;
    });


// sends reminders to users assigned to a task when it 3 days from it enddate
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


// send noti to group of members left or removed from group
exports.sendMemberRemovalNotification = functions.firestore
    .document("groups/{groupId}")
    .onUpdate(async (change, context) => {
      const beforeData = change.before.data();
      const afterData = change.after.data();

      // Check if the members list has changed
      if (beforeData.members.length > afterData.members.length) {
        // Determine the removed user
        const removedUserAuthId = beforeData.members
            .filter((user) => !afterData.members.includes(user))[0];

        try {
          // Retrieve the user's userName from the Users collection
          const userSnapshot = await admin.firestore().collection("Users")
              .where("userId", "==", removedUserAuthId).get();
          if (!userSnapshot.empty) {
            const userData = userSnapshot.docs[0].data();
            const { userName } = userData;

            // Construct the notification payload
            const payload = {
              notification: {
                title: "Member Removed from Group",
                body: `${userName} has been removed from the group.`,
              },
              data: {
                groupId: context.params.groupId,
                click_action: "OPEN_GROUP_ACTIVITY",
              },
            };

            // Send the notification to remaining group members
            const groupMembers = afterData.members
                .filter((user) => user !== removedUserAuthId);
            const messagingPromises = groupMembers.map(async (user) => {
              const userSnap = await admin.firestore()
                  .collection("Users").where("userId", "==", user).get();
              if (!userSnap.empty) {
                const userDataInner = userSnap.docs[0].data();
                const { fcmToken } = userDataInner;
                if (fcmToken) {
                  return admin.messaging().sendToDevice(fcmToken, payload);
                }
              }
              return null;
            });

            await Promise.all(messagingPromises);
            return null;
          }
          console.log("document not found for userAuthID:", removedUserAuthId);
        } catch (error) {
          console.error("Error fetching user data:", error);
        }
      }
      return null;
    });


 const axios = require("axios");

 exports.createRoom = functions.https.onRequest(async (req, res) => {
  const sdkToken = functions.config().agora.sdk_token;
  const options = {
    method: "POST",
    url: "https://api.netless.link/v5/rooms",
    headers: {
      "token": sdkToken,
      "Content-Type": "application/json",
      "region": "us-sv",
    },
    data: JSON.stringify({
      isRecord: false,
    }),
  };

  try {
    const response = await axios(options);
    res.send(response.data);
  } catch (error) {
    console.error(error);
    res.status(500).send("An error occurred while creating the room.");
  }
 });

 exports.generateToken = functions.https.onRequest(async (req, res) => {
  const roomUUID = "87cad3f0fc1a11ee8f6b69560a95c9aa";
  const sdkToken = functions.config().agora.sdk_token;

  const options = {
    method: "POST",
    url: `https://api.netless.link/v5/tokens/rooms/${roomUUID}`,
    headers: {
      "token": sdkToken,
      "Content-Type": "application/json",
      "region": "us-sv",
    },
    data: JSON.stringify({
      lifespan: 3600000,
      role: "admin",
    }),
  };

  try {
    const response = await axios(options);
    res.send(response.data);
  } catch (error) {
    console.error(error);
    res.status(500).send("An error occurred while generating the token.");
  }
 });

