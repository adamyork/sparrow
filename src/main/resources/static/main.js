document.game = {};
let startButton = document.getElementById("start");

window.onload = () => {
    let maxXVelocityRange = document.getElementById("maxXVelocityRange");
    document.getElementById("maxXVelocityRangeValue").textContent = maxXVelocityRange.value;
    maxXVelocityRange.addEventListener("change", () => {
        document.getElementById("maxXVelocityRangeValue").textContent = maxXVelocityRange.value;
    });
    let maxYVelocityRange = document.getElementById("maxYVelocityRange");
    document.getElementById("maxYVelocityRangeValue").textContent = maxYVelocityRange.value;
    maxYVelocityRange.addEventListener("change", () => {
        document.getElementById("maxYVelocityRangeValue").textContent = maxYVelocityRange.value;
    });
    let jumpDistanceRange = document.getElementById("jumpDistanceRange");
    document.getElementById("jumpDistanceRangeValue").textContent = jumpDistanceRange.value;
    jumpDistanceRange.addEventListener("change", () => {
        document.getElementById("jumpDistanceRangeValue").textContent = jumpDistanceRange.value;
    });
    let gravityRange = document.getElementById("gravityRange");
    document.getElementById("gravityRangeValue").textContent = gravityRange.value;
    gravityRange.addEventListener("change", () => {
        document.getElementById("gravityRangeValue").textContent = gravityRange.value;
    });
    let frictionRange = document.getElementById("frictionRange");
    document.getElementById("frictionRangeValue").textContent = frictionRange.value;
    frictionRange.addEventListener("change", () => {
        document.getElementById("frictionRangeValue").textContent = frictionRange.value;
    });
    let yVelocityCoefficientRange = document.getElementById("yVelocityCoefficientRange");
    document.getElementById("yVelocityCoefficientRangeValue").textContent = yVelocityCoefficientRange.value;
    yVelocityCoefficientRange.addEventListener("change", () => {
        document.getElementById("yVelocityCoefficientRangeValue").textContent = yVelocityCoefficientRange.value;
    })
    let xMovementDistanceRange = document.getElementById("xMovementDistanceRange");
    document.getElementById("xMovementDistanceRangeValue").textContent = xMovementDistanceRange.value;
    xMovementDistanceRange.addEventListener("change", () => {
        document.getElementById("xMovementDistanceRangeValue").textContent = xMovementDistanceRange.value;
    });
    let xAccelerationRateRange = document.getElementById("xAccelerationRateRange");
    document.getElementById("xAccelerationRateRangeValue").textContent = xAccelerationRateRange.value;
    xAccelerationRateRange.addEventListener("change", () => {
        document.getElementById("xAccelerationRateRangeValue").textContent = xAccelerationRateRange.value;
    });
    let xDeaccelerationRateRange = document.getElementById("xDeaccelerationRateRange");
    document.getElementById("xDeaccelerationRateRangeValue").textContent = xDeaccelerationRateRange.value;
    xDeaccelerationRateRange.addEventListener("change", () => {
        document.getElementById("xDeaccelerationRateRangeValue").textContent = xDeaccelerationRateRange.value;
    });
    let physicsForm = document.getElementById("physicsForm");
    physicsForm.addEventListener("submit", (event) => {
        event.preventDefault();
        const request = new Request("http://localhost:8080/physics", {
            method: "POST",
            headers: new Headers({'content-type': 'application/json'}),
            body: JSON.stringify({
                "maxXVelocityRange": parseFloat(maxXVelocityRange.value),
                "maxYVelocityRange": parseFloat(maxYVelocityRange.value),
                "jumpDistanceRange": parseFloat(jumpDistanceRange.value),
                "gravityRange": parseFloat(gravityRange.value),
                "frictionRange": parseFloat(frictionRange.value),
                "velocityYCoefficientRange": parseFloat(yVelocityCoefficientRange.value),
                "movementXDistanceRange": parseFloat(xMovementDistanceRange.value),
                "accelerationXRateRange": parseFloat(xAccelerationRateRange.value),
                "deaccelerationXRateRange": parseFloat(xDeaccelerationRateRange.value)
            })
        });
        fetch(request)
            .then((response) => {
                if (response.status !== 200) {
                    throw new Error("Something went wrong on API server!");
                }
            })
            .catch((error) => {
                console.error(error);
            });
    });
};

startButton.addEventListener("click", () => {

    document.getElementById("start").disabled = true;
    startButton.disabled = true;
    document.game.paused = false

    let splashContainer = document.getElementById("splashContainer");
    splashContainer.style.display = "none";

    document.game.lastFetchedScore = Date.now();
    document.game.imageLoader = new Image(1024, 168);

    document.game.drawCyclesCompleted = 0;
    document.game.updateFps = function () {
        console.log("updating fps")
        let fpsContainer = document.getElementById("fps");
        fpsContainer.innerHTML = (document.game.drawCyclesCompleted / 5) + "";
        document.game.drawCyclesCompleted = 0;
    }

    setInterval(document.game.updateFps, 5000);

    document.game.socket = new WebSocket("ws://localhost:8080/game");
    document.game.socket.addEventListener("open", () => {
        document.game.socket.send("START");
    });
    document.game.socket.addEventListener("message", (event) => {
        if (event.data.size === 0) {
            document.game.sendTime = Date.now();
            document.game.socket.send("NEXT");
            return
        }
        let blob = new Blob([event.data]);
        let maybeArrayBuffer = blob.arrayBuffer()
        maybeArrayBuffer.then(() => {
            let canvas = document.getElementById("game-window");
            let ctx = canvas.getContext("2d");
            document.game.imageLoader.src = URL.createObjectURL(blob);
            document.game.imageLoader.onload = function () {
                //console.log("one game tick" + (Date.now() - document.game.sendTime));
                document.game.loadedTime = Date.now();
                URL.revokeObjectURL(document.game.imageLoader.src);
                ctx.clearRect(0, 0, canvas.width, canvas.height);
                ctx.drawImage(document.game.imageLoader, 0, 0);
                document.game.sendTime = Date.now();
                document.game.socket.send("NEXT");
                document.game.gameAudioSocket.send("NEXT");
                document.game.drawCyclesCompleted++;
            };
        })
        if (Date.now() - document.game.lastFetchedScore >= 1000) {
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
                        document.game.lastFetchedScore = Date.now();
                    })
                })
        }
    });

    document.game.inputSocket = new WebSocket("ws://localhost:8080/input");
    document.onkeydown = function (event) {
        if (!document.game.paused) {
            console.log("keydown " + event.type + ":" + event.code)
            document.game.inputSocket.send(event.type + ":" + event.code);
            document.game.inputAudioSocket.send(event.type + ":" + event.code);
        }
    }
    document.onkeyup = function (event) {
        if (!document.game.paused) {
            console.log("keyup " + event.type + ":" + event.code)
            document.game.inputSocket.send(event.type + ":" + event.code);
        }
    }

    document.game.inputAudioSocket = new WebSocket("ws://localhost:8080/input-audio");
    document.game.inputAudioSocket.addEventListener("open", () => {
        console.log("START AUDIO");
        // let bgAudio = document.getElementById("bg-music");
        // bgAudio.loop = true;
        // bgAudio.play();
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

    document.game.backgroundMusicAudioSocket = new WebSocket("ws://localhost:8080/background-audio");
    document.game.backgroundMusicAudioSocket.addEventListener("open", () => {
        document.game.backgroundMusicAudioSocket.send("NEXT");
    });
    document.game.backgroundMusicAudioSocket.addEventListener("message", (event) => {
        let blob = new Blob([event.data], {'type': 'audio/wav'});
        if (blob.size !== 0) {
            let player = document.getElementById("bg-music");
            player.addEventListener("ended", () => {
                URL.revokeObjectURL(this.src);
                document.game.backgroundMusicAudioSocket.send("NEXT");
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
