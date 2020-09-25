import {next_message, last_message_num, last_message} from "./input_messages.js"
import {bindWebSocketEvents} from "./websocket.js"
import {init_speech, in_english_voice, out_english_voice, speek} from "./speech.js"

///////////////////////// Inits Simulation ////////////////////////////////
let output;  // Div area for chat messages
let websocket; // the websocket
let bot; // the bot
// Bot user and script
const user = "me-user";
const rivet_file = "rivescripts/chat_room.rive"

function init_app(){
    // Starts clock
    let timer = setInterval(clock, 10); 
    // Area for writing messages
    output = document.getElementById("ms"); 
    // Starts bot
    init_bot(rivet_file); 
    // Buttom for speech activation
    let clickme = document.getElementById("clickme") // In some browsers a gesture is required
    // Inits speech
    init_speech()
    clickme.onclick = init_speech
    // Socket creation and binding event handlers
    websocket = bindWebSocketEvents({onOpen, onClose, onMessage, onError});
}
//////////////////// Simulates a bot (this should be done in server) ////////////////////////
// function init_bot(file){
    // function load_done(){
      // console.log("Bot has finished loading!");
      // bot.sortReplies();
      // console.log("Bot is ready for chat");
    // }
    
    // bot = new RiveScript();
    // bot.loadFile(file)
       // .then(load_done)
       // .catch(load_fail)
// }
// Replies and outputs message
// function bot_reply(msg){
    // if (bot){
        // bot.sortReplies();
        // return bot.reply(user, msg.toLowerCase());
    // }
    // else Promise.resolve('bot is unrechable')
// }

function load_fail(err){ // it should be doing something better
    console.log(err)
}
//////////////////////////////// Websocket //////////////////////////////////
let sending = 6                  // Max number of messages to be sent to the bot
let send_timer;                  // Timer for sending messages periodically
const sending_frequency = 5000;  // Milliseconds to send cycle

function send(message){
    append(line(message, 'blue'))
    websocket.send(message);
}

function onOpen(evt) { // Handler for open socket, starts timer for sending messages
    append(line('Connection is open', 'big green underline'));
    send_timer = setInterval(function(){
                                sending--
                                if (sending < 0){ // Stop sending
                                    clearInterval(send_timer)
                                    return
                                }
                                if (websocket.readystate != WebSocket.CLOSED){
                                    let {msg} = next_message(); // simulated chat message
                                    send(msg);
                                    speek(msg, in_english_voice)
                                }
                            
                            }
                , sending_frequency)

}

function onClose(evt){ // Handler of close sockect
    append(line('Connection is closed', 'big red underline'));
}

function onMessage(evt){ // Handler of socket message
    console.log(evt)
    // Simulates a bot message
    let date = new Date(evt.timeStamp)
    bot_reply( last_message() ) // Returns a Promise!
    .then(function(response){
            insert_line(response, date);
            return response
          })
    .then(function(response){
         speek(response, out_english_voice)   
    })
    .catch( function(err){
        response += " *err* "
        insert_line(response, date)
    })
}

function onError(evt){
    append(line('Error:' + evt.data, 'red big'));
}

////////////////////////////// Output functions ///////////////////////////////////////////
function insert_line(response, date){
    append(line(`${response} (after ${date.getSeconds()} sec)`, 'green'))
    if (sending <= 0){
        websocket.close();
    }
}

function append(message){
    let p = document.createElement("p");
    p.innerHTML = message;
    output.appendChild(p);
}


function line(message, cls){
    return `<span class="${cls}"> ${message} </span>`
}
////////////////// Binds load event for calling init ///////////////////////////////////////////
window.addEventListener("load", init_app, false);
//////////////////////////////////////////////////////////////////////////////////////////////  