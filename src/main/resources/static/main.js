document.game = {};
let startButton = document.getElementById("start");
startButton.addEventListener("click", () => {

    document.getElementById("start").disabled = true;
    startButton.disabled = true;
    document.game.paused = false

    let splashContainer = document.getElementById("splashContainer");
    splashContainer.style.display = "none";

    document.game.lastFetchedAudio = Date.now();
    document.game.lastDrawTime = Date.now();
    document.game.drawCyclesCompleted = 0;
    document.game.imageLoader = new Image(1024, 168);

    document.game.socket = new WebSocket("ws://localhost:8080/game");
    document.game.socket.addEventListener("open", () => {
        document.game.socket.send("START");
    });
    document.game.socket.addEventListener("message", (event) => {
        let blob = new Blob([event.data]);
        let maybeArrayBuffer = blob.arrayBuffer()
        maybeArrayBuffer.then(() => {
            let canvas = document.getElementById("game-window");
            let ctx = canvas.getContext("2d");
            document.game.imageLoader.src = URL.createObjectURL(blob);
            document.game.imageLoader.onload = function () {
                document.game.loadedTime = Date.now();
                URL.revokeObjectURL(document.game.imageLoader.src);
                ctx.drawImage(document.game.imageLoader, 0, 0);
                if (Date.now() - document.game.lastDrawTime >= 1000) {
                    console.log("FPS : " + document.game.drawCyclesCompleted);
                    document.game.lastDrawTime = Date.now();
                    document.game.drawCyclesCompleted = 0;
                }
                document.game.socket.send("NEXT");
                document.game.gameAudioSocket.send("NEXT");
                document.game.drawCyclesCompleted++;
            };
        });
        if (Date.now() - document.game.lastFetchedAudio >= 1000) {
            let total = document.getElementById("total");
            let remaining = document.getElementById("remaining");
            const request = new Request("http://localhost:8080/score", {
                method: "GET"
            });
            fetch(request)
                .then((response) => {
                    response.text().then(text => {
                        let score = JSON.parse(text)
                        total.innerHTML = score["total"];
                        remaining.innerHTML = score["remaining"]
                        document.game.lastFetchedAudio = Date.now();
                    })
                })
        }
    });

    document.game.inputSocket = new WebSocket("ws://localhost:8080/input");
    document.onkeydown = function (event) {
        if (!document.game.paused) {
            document.game.inputSocket.send(event.type + ":" + event.code);
            document.game.inputAudioSocket.send(event.type + ":" + event.code);
        }
    }
    document.onkeyup = function (event) {
        if (!document.game.paused) {
            document.game.inputSocket.send(event.type + ":" + event.code);
        }
    }

    document.game.inputAudioSocket = new WebSocket("ws://localhost:8080/input-audio");
    document.game.inputAudioSocket.addEventListener("open", () => {
        console.log("START AUDIO");
        let bgAudio = document.getElementById("bg-music");
        bgAudio.loop = true;
        bgAudio.play();
    });
    document.game.inputAudioSocket.addEventListener("message", (event) => {
        let blob = new Blob([event.data], {'type': 'audio/wav'});
        let audioURL = window.URL.createObjectURL(blob);
        let player = document.getElementById("audio-player");
        player.addEventListener("ended", () => {
            URL.revokeObjectURL(this.src);
        })
        player.src = audioURL;
        player.play();
    });

    document.game.gameAudioSocket = new WebSocket("ws://localhost:8080/game-audio");
    document.game.gameAudioSocket.addEventListener("open", () => {
        document.game.gameAudioSocket.send("NEXT");
    });

    document.game.gameAudioSocket.addEventListener("message", (event) => {
        let blob = new Blob([event.data], {'type': 'audio/wav'});
        if (blob.size !== 0) {
            let player = document.getElementById("fx-player");
            player.addEventListener("ended", () => {
                URL.revokeObjectURL(this.src);
            })
            player.src = window.URL.createObjectURL(blob);
            if (!player.duration > 0) {
                player.play();
            }
        }
    });
});

let pauseButton = document.getElementById("pause");
pauseButton.addEventListener("click", () => {
    if (document.game.paused) {
        document.game.paused = false;
        document.game.socket.send("RESUME");
        let bgAudio = document.getElementById("bg-music");
        bgAudio.play();
        document.getElementById("pause").innerHTML = "PAUSE";
        document.getElementById("pause").blur()
        console.log("RESUME");
    } else {
        document.game.paused = true;
        document.game.socket.send("PAUSE");
        let bgAudio = document.getElementById("bg-music");
        bgAudio.pause();
        document.getElementById("pause").innerHTML = "RESUME";
        console.log("PAUSE");
    }
});
