document.game = {};
let startButton = document.getElementById("start");
startButton.addEventListener("click", (event) => {
    document.onkeydown = function (event) {
        document.game.socket.send(event.type + ":" + event.code);
        document.game.audioSocket.send(event.type + ":" + event.code);
    }
    document.onkeyup = function (event) {
        document.game.socket.send(event.type + ":" + event.code);
    }
    document.game.socket = new WebSocket("ws://localhost:8080/game");
    document.game.audioSocket = new WebSocket("ws://localhost:8080/input-audio");
    document.game.socket.addEventListener("open", (event) => {
        document.game.socket.send("START");
    });
    document.game.audioSocket.addEventListener("open", (event) => {
        console.log("START AUDIO");
        let bgAudio = document.getElementById("bg-music");
        bgAudio.loop = true;
        bgAudio.play();
    });
    startButton.disabled = true;
    document.game.imageLoader = new Image(1024, 168);
    document.game.socket.addEventListener("message", (event) => {
        let blob = new Blob([event.data]);
        let stuff = blob.arrayBuffer()
        stuff.then(value => {
            document.game.bufferedTime = Date.now();
            //console.log(value);
            let canvas = document.getElementById("game-window");
            let ctx = canvas.getContext("2d");
            document.game.imageLoader.src = URL.createObjectURL(blob);
            document.game.imageLoader.onload = function () {
                document.game.loadedTime = Date.now();
                URL.revokeObjectURL(this.src);
                ctx.drawImage(document.game.imageLoader, 0, 0);
                document.game.drawnTime = Date.now();
            };
        });
        let total = document.getElementById("total");
        let remaining = document.getElementById("remaining");
        const request = new Request("http://localhost:8080/score", {
            method: "GET"
        });
        fetch(request)
            .then((response) => {
                response.text().then(text => {
                    let obj = JSON.parse(text)
                    total.innerHTML = obj.total;
                    remaining.innerHTML = obj.remaining;
                })
            })
    });
    document.game.audioSocket.addEventListener("message", (event) => {
        let blob = new Blob([event.data], {'type': 'audio/wav'});
        let audioURL = window.URL.createObjectURL(blob);
        let player = document.getElementById("audio-player");
        player.addEventListener("ended", (event) => {
            URL.revokeObjectURL(this.src);
        })
        player.src = audioURL;
        player.play();
    });
    document.game.fxSocket = new WebSocket("ws://localhost:8080/game-audio");
    document.game.fxSocket.addEventListener("open", (event) => {
        document.game.fxSocket.send("FX:FX");
    });
    document.game.fxPlayer = document.getElementById("fx-player");
    document.game.fxPlayer.addEventListener("ended", (event) => {
        URL.revokeObjectURL(this.src);
    })
    document.game.fxSocket.addEventListener("message", (event) => {
        let blob = new Blob([event.data], {'type': 'audio/wav'});
        document.game.fxPlayer.src = window.URL.createObjectURL(blob);
        if (!document.game.fxPlayer.duration > 0) {
            document.game.fxPlayer.play();
        }
    });
});
