Incident Reporting and Monitoring System

Overview

The Incident Reporting and Monitoring System is a web-based application designed to enable citizens to report environmental and public safety incidents while allowing authorities to review, manage, and resolve them efficiently.

The system supports a complete workflow from incident submission to resolution, making it a full incident lifecycle management platform rather than a simple reporting tool.

Description

This platform improves communication between citizens and public authorities in Ifrane by providing a structured and transparent process for handling incidents.

It allows users to submit reports with supporting evidence and location data, while authorities can validate, assign, and monitor incidents until they are resolved.

Features

Citizen Features

Submit incident reports through a simple web interface

Upload photo evidence (JPG, JPEG, PNG)

Select incident location using an interactive map

Track the status of submitted reports

Submit reports anonymously or as an authenticated user

Authority Features

Access a centralized dashboard displaying all incidents

Filter incidents by category, status, date, and location

Review incident details including images and geolocation

Verify or reject submitted reports

Assign incidents to relevant departments or personnel

Update incident status throughout its lifecycle

Add internal notes and investigation comments

View aggregated statistics and summaries

Incident Workflow

Citizen submits an incident report

System validates input and checks for duplicates

Incident is stored in the database

Authority reviews the report

Incident is verified or rejected

Verified incidents are assigned to a responsible entity

Status is updated until the incident is resolved

Architecture

Architectural Pattern: Model-View-Controller (MVC)

Backend: RESTful API

Database: PostgreSQL

Frontend: Web-based interface


External Integrations

Geolocation API for map and location services

Media storage service for handling uploaded images

System Capabilities

Duplicate incident detection within a 300-meter radius over a 24-hour period

Incident status tracking (Submitted, Under Review, Verified, Rejected, In Progress, Resolved)

Audit logging for all major system actions

Data retention for a minimum of three years

Team
Marwa Errahmani
Aicha Labyad
Imane Chnigar
Kenza Benjelloun
Supervisor: Dr. Chakiri Houda
