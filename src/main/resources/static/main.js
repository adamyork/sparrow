document.game = {};
let startButton = document.getElementById("start");
startButton.addEventListener("click", (event) => {
    document.onkeydown = function (event) {
        document.game.socket.send(event.type + ":" + event.code);
    }
    document.onkeyup = function (event) {
        document.game.socket.send(event.type + ":" + event.code);
    }
    document.game.socket = new WebSocket("ws://localhost:8080/game");
    document.game.socket.addEventListener("open", (event) => {
        document.game.socket.send("START");
    });
    startButton.disabled = true;
    document.game.imageLoader = new Image(1024, 168);
    document.game.socket.addEventListener("message", (event) => {
        document.game.messageTime = Date.now();
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
                //ctx.clearRect(0, 0, canvas.width, canvas.height);
                ctx.drawImage(document.game.imageLoader, 0, 0);
                document.game.drawnTime = Date.now();
                //console.log(document.game.drawnTime - document.game.lastKeyDownTime);
            };
        });
    });
});
