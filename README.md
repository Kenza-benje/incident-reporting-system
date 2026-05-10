# Incident Reporting and Monitoring System

## 1. Project Overview

The Incident Reporting and Monitoring System is a web-based platform that enables citizens to report environmental and public safety incidents in Ifrane, while allowing authorities to review, manage, assign, and resolve them efficiently.

The system supports a complete incident lifecycle, from citizen submission to final resolution by the responsible authority.

## 2. Problem Statement

In many cities, communication between citizens and authorities regarding public safety and environmental issues is inefficient, unstructured, and lacks transparency.

This platform solves that problem by centralizing incident reporting, improving follow-up, and allowing authorities to manage incidents in a structured way.

## 3. Solution Description

Citizens can submit incident reports with descriptions, categories, photos, and geolocation data. Authorities can review reports, verify or reject them, assign them to departments, update their status, and resolve them.

## 4. Technologies Used

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Thymeleaf
- PostgreSQL
- HTML
- CSS
- JavaScript
- OpenStreetMap
- Gradle
- GitHub

## 5. Main Features

### Citizen Interface

- Submit incident reports
- Upload photo evidence
- Select incident location using an interactive map
- Track submitted reports
- Submit reports as a registered user or anonymously

### Authority Interface

- Access an authority dashboard
- View all submitted incidents
- Filter incidents by category, status, date, and location
- Review incident details
- Verify or reject reports
- Assign incidents to responsible departments or personnel
- Update incident status
- Add internal notes and investigation details
- View statistics and summaries

## 6. Incident Lifecycle

The system follows this workflow:

1. Citizen submits an incident report.
2. The system validates the report.
3. The system stores the report in the database.
4. Authority reviews the report.
5. Authority verifies or rejects the report.
6. Verified reports are assigned to the appropriate department.
7. The incident status is updated during treatment.
8. The incident is resolved.

Example status flow:

Submitted → Under Review → Verified → Assigned → In Progress → Resolved

Rejected is used when the report is invalid.

## 7. System Architecture

The project follows the MVC architecture.

- Model: Represents entities such as Incident, User, Status, and Category.
- View: Thymeleaf HTML pages used to display the interface.
- Controller: Handles user requests and connects the views with backend logic.
- Service: Contains the business logic of the application.
- Repository: Communicates with the PostgreSQL database.

## 8. Contribution History

This project was developed by a group of four members. Each member was responsible for a specific part of the system, while the group also collaborated on integration, testing, documentation, and the final video demo.

| Member | Main Contribution |
|---|---|
| Kenza Benjelloun | Worked on the authority side, including the authority dashboard, incident review pages, incident management interface, and authority workflow. |
| Marwa Errahmani | Worked on the citizen side, including the report submission interface, citizen pages, and user interaction for submitting and tracking incidents. |
| Imane Chnigar | Worked on the backend, including controllers, services, incident processing logic, and integration between the frontend and backend. |
| Aicha Labyad | Worked on the database part, including database design, entity relationships, PostgreSQL configuration, and data persistence. |

The project was completed collaboratively, with all members contributing to testing, debugging, documentation, and preparation of the final demo.

## 9. Setup Instructions

### Step 1: Clone the repository

```bash
git clone https://github.com/Kenza-benje/incident-reporting-system.git
cd incident-reporting-system
git checkout alertify-backend
