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