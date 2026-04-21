1. Project Overview

The Incident Reporting and Monitoring System is a web-based platform that enables citizens to report environmental and public safety incidents while allowing authorities to review, manage, and resolve them efficiently.

Unlike basic reporting tools, this system supports a complete incident lifecycle, from submission to resolution, ensuring structured handling and follow-up of each case.

2. Problem Statement

In many cities, communication between citizens and authorities regarding environmental and safety issues is inefficient, unstructured, and lacks transparency.

This platform addresses these challenges by providing a centralized system for reporting, tracking, and managing incidents in a reliable and organized manner.

3. Solution Description

The system improves coordination between citizens and public authorities in Ifrane by introducing a structured workflow.

Citizens can submit reports with supporting evidence and geolocation data, while authorities can validate, assign, and monitor incidents until resolution.

4. Core Functionalities
4.1 Citizen Interface
Submit incident reports through a user-friendly web form
Upload photo evidence (JPG, JPEG, PNG)
Select incident location via an interactive map
Track the status of submitted reports
Submit reports anonymously or as a registered user
4.2 Authority Interface
Access a centralized dashboard of all incidents
Filter incidents by category, status, date, and location
Review detailed incident information (description, images, location)
Verify or reject submitted reports
Assign incidents to departments or responsible personnel
Update incident status throughout its lifecycle
Add internal notes and investigation details
View statistical summaries and reports
5. Incident Lifecycle Workflow

The system follows a structured workflow:

Report submission by the citizen
Input validation and duplicate detection
Storage of the incident in the database
Review by authorized personnel
Verification or rejection of the incident
Assignment to the appropriate authority
Status updates until final resolution
6. System Architecture
6.1 Technical Architecture
Architectural Pattern: Model-View-Controller (MVC)
Backend: RESTful API
Database: PostgreSQL
Frontend: Web-based application
6.2 External Services
Geolocation API for mapping and location selection
Media storage service for managing uploaded images
7. Key System Capabilities
Duplicate incident detection (within 300 meters over 24 hours)
Multi-stage status tracking:
Submitted
Under Review
Verified
Rejected
In Progress
Resolved
Audit logging of all major actions
Data retention policy (minimum of three years)
8. Non-Functional Highlights
Secure communication using HTTPS
Password hashing and secure authentication
Role-based access control
High performance (response time under 2 seconds)
Support for concurrent users
Automated database backups
9. Project Team
Marwa Errahmani
Aicha Labyad
Imane Chnigar
Kenza Benjelloun

Supervisor: Dr. Chakiri Houda

10. Future Enhancements
Notification system (email/SMS alerts)
AI-based incident classification
Severity prioritization
Advanced analytics dashboard
Intelligent assignment mechanisms
