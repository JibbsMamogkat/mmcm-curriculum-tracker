const {onRequest} = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

admin.initializeApp();

const EMAIL = "mmcmctracker@gmail.com";
const PASSWORD = "trcrpppxotfhqguq";

const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: EMAIL,
    pass: PASSWORD,
  },
});

exports.sendOtpEmail = onRequest(
  { timeoutSeconds: 60, memory: "256MB" },
  async (req, res) => {
    console.log("sendOtpEmail function called with body:", JSON.stringify(req.body));

    const email = req.body.email ? req.body.email.trim() : "";
    const otp = req.body.otp ? req.body.otp.toString().trim() : "";
    if (!email || !otp) {
      console.error("Missing email or OTP.");
      res.status(400).json({ success: false, message: "Error: Missing email or OTP" });
      return;
    }
    try {
      console.log(`Sending OTP: ${otp} to email: ${email}`);
      const mailOptions = {
        from: "MMCM CURRICULUM TRACKER <mmcmctracker@gmail.com>",
        to: email,
        subject: "OTP CODE FOR MMCMcTracker",
        text: `Your OTP code is: ${otp}. This code is valid for 5 minutes.`,
      };
      const info = await transporter.sendMail(mailOptions);
      console.log(`Email sent: ${info.messageId}`);
      res.json({ success: true, message: "OTP sent successfully!" });
    } catch (error) {
      console.error("Error sending email:", error);
      res.status(500).json({ success: false, message: error.toString() });
    }
  }
);
