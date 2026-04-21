Incident Reporting and Monitoring System

A web-based platform that enables citizens to report environmental and public safety incidents while allowing authorities to review, manage, and resolve them efficiently.

📖 Description

The Incident Reporting and Monitoring System is designed to improve communication between citizens and public authorities in Ifrane.
It provides a complete workflow from incident reporting → verification → assignment → resolution, making it more than just a reporting tool — it is a full incident lifecycle management system.

✨ Features
👤 Citizen Features
Report incidents بسهولة (simple web form)
Upload photo evidence (JPG, PNG)
Select location via interactive map 📍
Track report status in real-time
Submit reports anonymously or as a registered user
🏢 Authority Features
Dashboard with all incidents
Filter by category, status, date, and location
Verify or reject reports
Assign incidents to departments
Update incident status (workflow management)
Add internal notes and comments
View statistics and summaries 📊
🧭 Incident Workflow
Citizen submits report
        ↓
System validates data + checks duplicates
        ↓
Incident stored in database
        ↓
Authority reviews report
        ↓
Verified / Rejected
        ↓
Assigned to responsible department
        ↓
Status updated until RESOLVED
🏗️ Architecture
Architecture Pattern: MVC (Model-View-Controller)
Backend: REST API
Database: PostgreSQL
Frontend: Web Application
🔌 External Services
Geolocation API (maps & location)
Media Storage Service (image uploads)
🔐 Security & Performance
HTTPS (secure communication)
Password hashing (no plain text storage)
Role-based access control
Account lock after multiple failed logins
⚡ Fast response time (< 2 seconds for most operations)
📈 Supports 300+ concurrent users
🔄 Daily database backups
📊 System Capabilities
Duplicate incident detection (300m radius / 24h)
Incident status tracking:
Submitted
Under Review
Verified
Rejected
In Progress
Resolved
Audit logging
Data retention (minimum 3 years)
🖼️ Wireframes (UI Design)

👉 https://www.figma.com/make/T2M5xM9IkzdC8xLgW37W2Y

📁 Project Structure (Suggested)
incident-reporting-system/
│
├── backend/
│   ├── controllers/
│   ├── models/
│   ├── routes/
│   └── services/
│
├── frontend/
│   ├── components/
│   ├── pages/
│   └── assets/
│
├── database/
│   └── schema.sql
│
├── docs/
│   └── diagrams/
│
├── README.md
└── .gitignore
👥 Team
Aicha Labyad
Imane Chnigar
Kenza Benjelloun
Marwa Errahmani

Supervisor: Dr. Chakiri Houda
