const express = require('express')
const seedrandom = require('seedrandom')

const app = express()
const port = 3000

app.use(express.json())

app.post('/api/verify', (req, res) => {
  console.log(req.body);
  // verify simple key
  let data = req.body;
  if (data["randomseed"] === undefined ||
    data["randoms"] === undefined ||
    data["traces"] === undefined) {
    res.send("invalid verify format");
    return;
  }

  // verify random seed array
  let rng = seedrandom.alea(data["randomseed"], {
    state: true
  });

  let r_array = data["randoms"]
  for (let index = 0; index < r_array.length; index++) {
    const rnd = r_array[index];
    const rnd_expected = rng();
    if (rnd != rnd_expected) {
      console.log("give rnd", rnd);
      console.log("calculate rnd", rnd_expected);
      res.send("randomseed is not match with the randoms");
      return;
    }
  }

  // simple game logic replay
  let items = {
    "study": 0, "insight": 0, "draftExp": 0, "tunedExp": 0, "flag": 0,
    "submittedFlag": 0, "wrongFlag": 0, "resubmittedFlag": 0,
  };
  let qualified = true;
  let t_array = data["traces"];
  let rndCursor = 0;

  let processEvent = null;
  for (let index = 0; index < t_array.length; index++) {
    let t = t_array[index];

    if (t[0] == 'event') {
      processEvent = t[1][0];
      console.log("iterate event", processEvent);
    }
    else if (t[0] == 'action') {
      let actionId = t[1][0];
      let metadata = t[1][1];
      if (actionId === undefined || actionId === null ||
        metadata === undefined || metadata === null) {
        res.send("internal error");
        return;
      }

      console.log("iterate action", actionId, metadata)
      switch (actionId) {
        case 'Log':
        case 'DisplayMessage':
          break;
        case 'DisplayRandomMessage':
          if (processEvent != 'HourBeginTasks') {
            let msgRnd = metadata[0];
            let msgId = metadata[1];
            rndExpected = r_array[rndCursor];
            if (msgRnd != rndExpected) {
              console.log("DisplayRandomMessage bad random, calculate", rndExpected, "said", msgRnd);
              res.send("DisplayRandomMessage bad random, are you cheating?");
              return;
            }
            rndCursor += 1;
          }
          break;
        case 'DisplayChoices':
          if (processEvent == 'TheBeginning')
            break
          else if (processEvent == 'HourBeginTasks') {
            choice = metadata[0]

            if (choice < 0 || choice > 7) {
              console.log("choice underflow or overflow");
              res.send("Bad choice, are you cheating?");
              return;
            }

            switch (choice) {
              case 0:
                items["study"] += 1;
                break;
              case 1: {
                let next_trace = t_array[index + 1];
                if (next_trace[0] != 'action') {
                  console.log("next trace is not action");
                  res.send("Bad logic, are you cheating?");
                  return;
                }
                let sub_action = next_trace[1];
                let sub_actionId = sub_action[0];
                let sub_metadata = sub_action[1];
                if (sub_actionId !== "CoinFlip") {
                  console.log("next action is not CoinFlip");
                  res.send("Bad logic, are you cheating?");
                  return;
                }
                let flipRes = sub_metadata[0];
                rndExpected = r_array[rndCursor];
                if (rndExpected != flipRes) {
                  console.log("CoinFlip bad random, calculate", rndExpected, "said", flipRes);
                  res.send("CoinFlip bad random, are you cheating?");
                  return;
                }
                rndCursor += 1;
                if (flipRes < 0.6) {
                  items["insight"] += 1;
                  rndCursor += 2;
                }
                else {
                  rndCursor += 1;
                }
                break;
              }
              case 2: {
                if (items["insight"] <= 0) {
                  console.log("no enough insights");
                  res.send("Have no insights, are you cheating?");
                  return;
                }
                let next_trace = t_array[index + 1];
                if (next_trace[0] != 'action') {
                  console.log("next trace is not action");
                  res.send("Bad logic, are you cheating?");
                  return;
                }
                let sub_action = next_trace[1];
                let sub_actionId = sub_action[0];
                let sub_metadata = sub_action[1];
                if (sub_actionId !== "CoinFlip") {
                  console.log("next action is not CoinFlip");
                  res.send("Bad logic, are you cheating");
                  return;
                }
                let flipRes = sub_metadata[0];
                rndExpected = r_array[rndCursor];
                if (rndExpected != flipRes) {
                  console.log("CoinFlip bad random, calculate", rndExpected, "said", sub_metadata);
                  res.send("CoinFlip bad random, are you cheating?");
                  return;
                }
                rndCursor += 1;
                if (flipRes < 0.8) {
                  items["draftExp"] += 1;
                }
                else
                  rndCursor += 1;
                items["insight"] -= 1;
                break;
              }
              case 3: {
                if (items["draftExp"] <= 0) {
                  console.log("no enough draftExp");
                  return;
                }
                let next_trace = t_array[index + 1];
                if (next_trace[0] != 'action') {
                  console.log("next trace is not action");
                  res.send("Bad logic, are you cheating");
                  return;
                }
                let sub_action = next_trace[1];
                let sub_actionId = sub_action[0];
                let sub_metadata = sub_action[1];
                if (sub_actionId !== "CoinFlip") {
                  console.log("next action is not CoinFlip");
                  res.send("Bad logic, are you cheating");
                  return;
                }
                let flipRes = sub_metadata[0];
                rndExpected = r_array[rndCursor];
                if (rndExpected != flipRes) {
                  console.log("CoinFlip bad random, calculate", rndExpected, "said", sub_metadata);
                  res.send("CoinFlip bad random, are you cheating?");
                  return;
                }
                rndCursor += 1;
                if (flipRes < 0.7) {
                  items["draftExp"] -= 1;
                  items["tunedExp"] += 1;
                }
                else {
                  rndCursor += 1;
                }
                break;
              }
              case 4: {
                if (items["tunedExp"] <= 0) {
                  console.log("no enough tunedExp");
                  res.send("Have no tunedExp, are you cheating?");
                  return;
                }
                let next_trace = t_array[index + 1];
                if (next_trace[0] != 'action') {
                  console.log("next trace is not action");
                  res.send("Bad logic, are you cheating?");
                  return;
                }
                let sub_action = next_trace[1];
                let sub_actionId = sub_action[0];
                let sub_metadata = sub_action[1];
                if (sub_actionId !== "CoinFlip") {
                  console.log("next action is not CoinFlip");
                  res.send("Bad logic, are you cheating?");
                  return;
                }
                let flipRes = sub_metadata[0];
                rndExpected = r_array[rndCursor];
                if (rndExpected != flipRes) {
                  console.log("CoinFlip bad random, calculate", rndExpected, "said", sub_metadata);
                  res.send("CoinFlip bad random, are you cheating?");
                  return;
                }
                rndCursor += 1;
                if (flipRes < 0.7) {
                  items["tunedExp"] -= 1;
                  items["submittedFlag"] += 1;
                }
                else {
                  rndCursor += 1;
                }
                break;
              }
              case 5: {
                if (items["wrongFlag"] <= 0) {
                  console.log("no enough wrongFlag");
                  res.send("Have no wrongFlag, are you cheating?");
                  return;
                }
                items["wrongFlag"] -= 1;
                items["resubmittedFlag"] += 1;
                break;
              }
              default:
                break;
              case 6:
                // random
                let next_trace = t_array[index + 1];
                if (next_trace[0] != 'action') {
                  console.log("next trace is not action");
                  res.send("Bad logic, are you cheating?");
                  return;
                }
                let sub_action = next_trace[1];
                let sub_actionId = sub_action[0];
                let sub_metadata = sub_action[1];
                if (sub_actionId !== "Random") {
                  console.log("next action is not Random");
                  res.send("Bad logic, are you cheating?");
                  return;
                }
                let randomRes = sub_metadata[0];
                rndExpected = r_array[rndCursor];
                if (rndExpected != randomRes) {
                  console.log("Random bad random, calculate", rndExpected, "said", sub_metadata);
                  res.send("Random bad random, are you cheating?");
                  return;
                }
                rndCursor += 1;
                let sumRnd = randomRes * 3;
                if (sumRnd < 1) {
                  rndCursor += 1;
                }
            }
            break;
          }
        case 'Random':
          if (processEvent != 'HourBeginTasks') {
            rndExpected = r_array[rndCursor];
            let weightRnd = metadata[0];
            if (weightRnd != rndExpected) {
              console.log("Random bad random, calculate", rndExpected, "said", sub_metadata);
              res.send("Random bad random, are you cheating?");
              return;
            }
            rndCursor += 1;
            break;
          }
        default:
          break;
        case 'CoinFlip':
          if (rndExpected < 'Qualify') {
            rndExpected = r_array[rndCursor];
            let flipRnd = metadata[0];
            if (rndExpected != flipRnd) {
              console.log("CoinFlip bad random, calculate", rndExpected, "said", flipRnd);
              res.send("CoinFlip bad random, are you cheating?");
              return;
            }
            if (flipRnd >= 0.25 * items['study'])
              qualified = false;
            rndCursor += 1;
          }
          else if (processEvent == 'HourBeginTasks') {}
          else if (processEvent == 'flagCheck') {
            if (items["submittedFlag"] <= 0) {
              console.log("CoinFlip bad random, calculate", rndExpected, "said", flipRnd);
              res.send("CoinFlip bad random, are you cheating?");
              return;
            }
            rndExpected = r_array[rndCursor];
            let flipRnd = metadata[0];
            if (rndExpected != flipRnd) {
              console.log("CoinFlip bad random, calculate", rndExpected, "said", flipRnd);
              res.send("CoinFlip bad random, are you cheating?");
              return;
            }
            if (flipRnd < 0.6) {
              items["submittedFlag"] -= 1;
              items["flag"] += 1;
              processEvent = "flagChecked";
            }
            else {
              items["submittedFlag"] -= 1;
              items["wrongFlag"] += 1;
            }
            rndCursor += 1;
            break;
          }
          else if (processEvent == 'ResubmittedFlagCheck') {
            if (items["resubmittedFlag"] <= 0) {
              console.log("CoinFlip bad random, calculate", rndExpected, "said", flipRnd);
              res.send("CoinFlip bad random, are you cheating?");
              return;
            }
            rndExpected = r_array[rndCursor];
            let flipRnd = metadata[0];
            if (rndExpected != flipRnd) {
              console.log("CoinFlip bad random, calculate", rndExpected, "said", flipRnd);
              res.send("CoinFlip bad random, are you cheating?");
              return;
            }
            if (flipRnd < 0.85) {
              items["resubmittedFlag"] -= 1;
              items["flag"] += 1;
            }
            rndCursor += 1;
            break;
          }
          else {
            rndCursor += 1;
            break;
          }
          break;
        case 'Switch':
        case 'Loop':
        case 'UpdateVariable':
        case 'UpdateVariables':
        case 'UpdateVariableLimits':
        case 'GiveItem':
        case 'GiveItem':
        case 'GiveItem':
        case 'SetStatus':
        case 'TriggerEvents':
          break;
      }
    }
    else {
      res.send("internal error");
      return;
    }
  }

  console.log("rnd", rndCursor);
  console.log('items', items);

  if (items["flag"] === undefined || items["flag"] < 8 || !qualified) {
    res.send("You didn't win the flag");
    return;
  }

  res.send("This is your flag: " + process.env.FLAG);
})

app.use('/static', express.static('static'))

app.listen(port, () => {
  console.log(`Example app listening on port ${port}`)
})
