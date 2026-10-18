/* =====================================================
   ESEC SIMS — CINEMATIC INTERACTION ENGINE
===================================================== */

/* =====================================================
   PARTICLES
===================================================== */

const particleContainer = document.getElementById("particles");
const PARTICLE_COUNT = 90;

if (particleContainer) {
    for (let i = 0; i < PARTICLE_COUNT; i++) {
        const particle = document.createElement("div");
        particle.className = "particle";
        particle.style.left = Math.random() * 100 + "%";
        particle.style.top = Math.random() * 100 + "%";
        particle.style.setProperty("--duration", `${5 + Math.random() * 10}s`);
        particle.style.setProperty("--move-x", `${-50 + Math.random() * 100}px`);
        particle.style.setProperty("--move-y", `${-60 + Math.random() * 120}px`);
        particle.style.animationDelay = `${Math.random() * 8}s`;

        const size = Math.random() < 0.8 ? 1.5 : 3;
        particle.style.width = `${size}px`;
        particle.style.height = `${size}px`;

        particleContainer.appendChild(particle);
    }
}

/* =====================================================
   MOUSE POSITION & CINEMATIC MOTION
===================================================== */

const heroMain = document.getElementById("heroMain");
const heroCopy = document.getElementById("heroCopy");
const loginCard = document.getElementById("loginCard");
const energyArc = document.querySelector(".energy-arc");
const ambientOne = document.querySelector(".ambient-one");
const ambientTwo = document.querySelector(".ambient-two");
const cubes = document.querySelectorAll(".cube");

let targetX = 0;
let targetY = 0;
let currentX = 0;
let currentY = 0;

document.addEventListener("mousemove", (event) => {
    targetX = event.clientX / window.innerWidth - 0.5;
    targetY = event.clientY / window.innerHeight - 0.5;
});

function cinematicMotion() {
    currentX += (targetX - currentX) * 0.025;
    currentY += (targetY - currentY) * 0.025;

    if (loginCard) {
        const rotateY = currentX * 6;
        const rotateX = currentY * -6;
        const translateX = currentX * 6;
        const translateY = currentY * 4;

        loginCard.style.transform = `
            translate3d(${translateX}px, ${translateY}px, 0)
            rotateX(${rotateX}deg)
            rotateY(${rotateY}deg)
        `;
    }

    if (heroCopy) {
        heroCopy.style.transform = `
            translate3d(${currentX * -10}px, ${currentY * -6}px, 0)
        `;
    }

    if (energyArc) {
        energyArc.style.marginLeft = `${currentX * 25}px`;
        energyArc.style.marginTop = `${currentY * 15}px`;
    }

    if (ambientOne) {
        ambientOne.style.transform = `translate(${currentX * -30}px, ${currentY * -20}px) scale(1.05)`;
    }

    if (ambientTwo) {
        ambientTwo.style.transform = `translate(${currentX * 20}px, ${currentY * 15}px)`;
    }

    cubes.forEach((cube, index) => {
        const strength = index === 0 ? 25 : -18;
        cube.style.marginLeft = `${currentX * strength}px`;
        cube.style.marginTop = `${currentY * strength}px`;
    });

    requestAnimationFrame(cinematicMotion);
}

cinematicMotion();

document.addEventListener("mouseleave", () => {
    targetX = 0;
    targetY = 0;
    if (loginCard) {
        loginCard.style.transform = "translate3d(0,0,0)";
    }
    if (heroCopy) {
        heroCopy.style.transform = "translate3d(0,0,0)";
    }
});

/* =====================================================
   STEP TRANSITION: LET'S START -> WELCOME BACK LOGIN BOX
===================================================== */

const startBtn = document.getElementById("startBtn");
const backBtn = document.getElementById("backBtn");

function openLoginScreen() {
    if (heroMain) {
        heroMain.classList.add("show-login");
    }
    clearLoginError();
    const userIdInput = document.getElementById("userId");
    if (userIdInput) {
        setTimeout(() => userIdInput.focus(), 350);
    }
}

function closeLoginScreen() {
    if (heroMain) {
        heroMain.classList.remove("show-login");
    }
    clearLoginError();
}

if (startBtn) {
    startBtn.addEventListener("click", openLoginScreen);
}

if (backBtn) {
    backBtn.addEventListener("click", closeLoginScreen);
}

/* =====================================================
   PASSWORD VISIBILITY TOGGLE (EYE ONLY - NO SIGN IN)
===================================================== */

const password = document.getElementById("password");
const showPassword = document.getElementById("showPassword");

if (showPassword && password) {
    showPassword.addEventListener("click", (e) => {
        e.preventDefault();
        e.stopPropagation();

        if (password.type === "password") {
            password.type = "text";
            showPassword.textContent = "👁";
            showPassword.title = "Hide password";
        } else {
            password.type = "password";
            showPassword.textContent = "○";
            showPassword.title = "Show password";
        }
    });
}

/* =====================================================
   ROLE SWITCHING
===================================================== */

const role = document.getElementById("role");
const idLabel = document.getElementById("idLabel");
const passwordLabel = document.getElementById("passwordLabel");
const userId = document.getElementById("userId");
const passwordInput = document.getElementById("password");

function updateRoleLabels() {
    clearLoginError();
    const val = (role.value || "").toLowerCase();

    if (val === "student") {
        idLabel.textContent = "ROLL NUMBER";
        passwordLabel.textContent = "REGISTERED MOBILE";
        userId.placeholder = "Enter roll number (e.g. ES25CS**)";
        passwordInput.placeholder = "Enter registered mobile ";
    } else if (val === "staff") {
        idLabel.textContent = "STAFF ID";
        passwordLabel.textContent = "PASSWORD";
        userId.placeholder = "Enter staff ID (e.g. STF***)";
        passwordInput.placeholder = "Enter password";
    } else {
        idLabel.textContent = "ADMINISTRATOR ID";
        passwordLabel.textContent = "PASSWORD";
        userId.placeholder = "Enter administrator ID (e.g. ADM***)";
        passwordInput.placeholder = "Enter administrator password";
    }
}

if (role) {
    role.addEventListener("change", updateRoleLabels);
    updateRoleLabels();
}

[userId, passwordInput].forEach(inp => {
    if (inp) {
        inp.addEventListener("input", clearLoginError);
        inp.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                document.getElementById("loginButton").click();
            }
        });
    }
});

/* =====================================================
   INLINE ERROR HANDLING (NO BROWSER POPUPS)
===================================================== */

function showLoginError(msg) {
    const errBox = document.getElementById("loginError");
    if (errBox) {
        errBox.textContent = msg;
        errBox.style.display = "block";
    }
}

function clearLoginError() {
    const errBox = document.getElementById("loginError");
    if (errBox) {
        errBox.textContent = "";
        errBox.style.display = "none";
    }
}

/* =====================================================
   LOGIN SUBMISSION (SINGLE SIGN IN BUTTON)
===================================================== */

const loginButton = document.getElementById("loginButton");

if (loginButton) {
    loginButton.addEventListener("click", async (e) => {
        e.preventDefault();
        clearLoginError();

        const selectedRole = role ? role.value.trim() : "";
        const id = userId ? userId.value.trim() : "";
        const credential = passwordInput ? passwordInput.value.trim() : "";

        if (!selectedRole || !id || !credential) {
            showLoginError("Please enter both ID/Roll number and Password/Mobile.");
            return;
        }

        const originalHTML = loginButton.innerHTML;
        loginButton.disabled = true;
        loginButton.innerHTML = `
            <span class="button-text">AUTHENTICATING...</span>
            <span class="button-arrow">◌</span>
        `;

        try {
            const response = await fetch("/api/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: new URLSearchParams({
                    role: selectedRole,
                    id: id,
                    credential: credential
                })
            });

            const result = await response.json();

            if (result.success) {
                localStorage.setItem("sims_name", result.name);
                localStorage.setItem("sims_role", result.role);
                localStorage.setItem("sims_id", result.id);

                loginButton.innerHTML = `
                    <span class="button-text">ACCESS GRANTED</span>
                    <span class="button-arrow">✓</span>
                `;

                setTimeout(() => {
                    if (result.role === "Student") {
                        window.location.href = "/dashboard.html";
                    } else if (result.role === "Staff") {
                        window.location.href = "/staff-dashboard.html";
                    } else if (result.role === "Administrator") {
                        window.location.href = "/admin-dashboard.html";
                    } else {
                        window.location.href = "/dashboard.html";
                    }
                }, 350);

            } else {
                showLoginError(result.message || "Invalid credentials. Please verify your details.");
                loginButton.disabled = false;
                loginButton.innerHTML = originalHTML;
            }

        } catch (error) {
            console.error("Login request failed:", error);
            showLoginError("Unable to connect to SIMS backend server (port 8080).");
            loginButton.disabled = false;
            loginButton.innerHTML = originalHTML;
        }
    });

    // Magnetic button hover effect on single Sign In button
    loginButton.addEventListener("mousemove", (event) => {
        const rect = loginButton.getBoundingClientRect();
        const x = event.clientX - rect.left - rect.width / 2;
        const y = event.clientY - rect.top - rect.height / 2;
        loginButton.style.transform = `translate(${x * 0.08}px, ${y * 0.08}px)`;
    });

    loginButton.addEventListener("mouseleave", () => {
        loginButton.style.transform = "translate(0,0)";
    });
}