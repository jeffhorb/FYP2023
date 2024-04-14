// First install the dependencies:
// npm install pusher express cors

const express = require("express");
const cors = require("cors");
const Pusher = require("pusher");
const pusher = new Pusher({
  appId: "1783865",
  key: "9b513f72eabe6535a761",
  secret: "90f4347a3ab3a02dde62",
  cluster: "eu",
  useTLS: true,
});
const app = express();

app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cors());
app.post("/pusher/user-auth", (req, res) => {
  const socketId = req.body.socket_id;

  // Replace this with code to retrieve the actual user id and info
  const user = {
    id: "foo-bar_1234@=,.",
    user_info: {
      name: "Jeff Horbs",
    },
    watchlist: ['09r-bar_1234@=,.', 'foo-opr_1234@=,.']
  };
  const authResponse = pusher.authenticateUser(socketId, user);
  res.send(authResponse);
});

const port = process.env.PORT || 5000;
app.listen(port);