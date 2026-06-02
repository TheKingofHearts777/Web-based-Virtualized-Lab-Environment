# Backend Architecture

The CyberLab backend is a Java Spring Boot application responsible for authentication, user management, course management, lab deployment, virtual machine provisioning, and Proxmox integration.

The system provides a REST API used by the frontend to manage users, assign labs, provision virtual machines, and access browser-based VM consoles.

## Technology Stack

### Core Framework

* Java 21
* Spring Boot 3.4.2
* Spring Web
* Spring Security
* Spring Data MongoDB
* Maven

### Database

* MongoDB
* Spring Data repositories
* Document-oriented data model

### Authentication

* JWT Access Tokens
* JWT Refresh Tokens
* BCrypt password hashing
* Stateless authentication

### Infrastructure

* Proxmox VE
* Proxmox REST API
* SFTP-based virtual disk uploads
* Browser-based VNC access

### Documentation

* OpenAPI / Swagger UI
* SpringDoc OpenAPI

## Backend Architecture

```text
Frontend (Angular)
        |
        v
Spring Boot REST API
        |
        +------------------+
        |                  |
        v                  v
    MongoDB           Proxmox VE
        |                  |
        |                  |
        +--------+---------+
                 |
                 v
         Virtual Machines
```

The backend acts as the central orchestration layer between users, course content, lab assignments, MongoDB persistence, and Proxmox virtualization resources.

## Security Model

Authentication is implemented using Spring Security and JWT tokens.

Each user is assigned one of three system roles:

* Admin
* Instructor
* Student

Authorization is enforced at both endpoint and resource levels. Users can only access resources they own or have permission to manage.

### Authentication Flow

1. User logs in.
2. Backend validates credentials.
3. JWT access token is issued.
4. Refresh token is stored.
5. Requests are authenticated through JWT filters.
6. Resource ownership is validated before returning data.

## Data Model

The system uses a document-oriented structure built around users.

```text
User
└── LabInstances
    └── VmInstances
```

Rather than storing labs and VM instances as independent collections, they are nested beneath their owning user.

This design simplifies:

* Authorization
* Ownership tracking
* Resource cleanup
* User-centric queries

## User Management

Users are stored as MongoDB documents.

Each user contains:

* Username
* Password hash
* User role
* Refresh token
* Last activity timestamp
* Assigned lab instances

Administrators can create user accounts through the API.

## Course Management

Courses provide organizational structure for:

* Students
* Instructors
* Assigned labs

Course information is referenced during lab assignment creation and validation.

## Lab Template System

Lab templates function as reusable blueprints.

A template contains:

* Name
* Description
* Objectives
* Questions
* VM template references

Templates are created once and assigned many times.

When assigned to a student, a LabInstance is generated from the template.

## Lab Instance System

Lab instances represent a student's individual copy of a lab.

Each lab instance stores:

* Template information
* Objectives
* Questions
* Student answers
* Completion status
* Due date
* Last access date
* Associated virtual machines

When a student submits answers:

* Answers are stored
* Lab is marked complete
* Additional modifications are prevented

## Virtual Machine Template System

VM Templates represent reusable machine images stored within Proxmox.

A template contains:

* Name
* Description
* Proxmox VM ID
* Proxmox node

VM templates are uploaded as VirtualBox VDI files and converted into reusable Proxmox templates.

### VM Template Upload Workflow

1. Instructor uploads a VDI file.
2. File header is validated.
3. File size is validated.
4. VDI is transferred via SFTP.
5. Proxmox imports the disk.
6. Base VM is cloned.
7. Uploaded disk replaces the default disk.
8. Template metadata is stored in MongoDB.

Maximum VDI size: 10 GB.

## VM Instance Provisioning

When a lab is assigned:

1. The Lab Template is cloned.
2. Associated VM Templates are identified.
3. Proxmox clones new VM instances.
4. VM metadata is stored in MongoDB.
5. Networking is automatically configured.

Each VM instance stores:

* MongoDB ID
* Proxmox VM ID
* VM Name
* Clone Date
* Parent Template ID
* Proxmox Node

## Virtualization Abstraction Layer

The backend uses a VirtualizationProvider interface to abstract infrastructure operations.

Supported operations include:

* Create VM Instance
* Delete VM Instance
* Create VM Template
* Delete VM Template
* Retrieve VNC Connection Information
* Configure Lab Networking
* Storage Monitoring

This abstraction allows future virtualization platforms to be added without rewriting business logic.

## Proxmox Integration

Proxmox is the primary virtualization platform.

Capabilities include:

### VM Template Management

* Template creation
* Template deletion
* Template cloning

### VM Instance Management

* Clone instances
* Destroy instances
* Retrieve VM information

### Browser-Based Console Access

The backend retrieves:

* VNC WebSocket endpoint
* Proxmox authentication ticket
* VNC port

These credentials allow the frontend to launch browser-based VM consoles.

## Automated Lab Networking

When a lab is deployed:

1. A Proxmox SDN Zone is created.
2. A VNet is created.
3. A subnet is created.
4. DHCP is configured.
5. VM network adapters are attached.
6. Unique MAC addresses are generated.

This gives every lab environment an isolated network segment.

## Storage Management

Before creating templates or instances, the backend verifies available storage capacity.

The system monitors:

* Image storage utilization
* Disk capacity
* Upload limits

Template creation is blocked when storage limits are exceeded.

## VM Garbage Collection

The platform includes an automated cleanup service.

A scheduled task runs daily at 2:00 AM.

Responsibilities include:

* Finding expired labs
* Finding expired VM instances
* Deleting Proxmox VMs
* Removing references from MongoDB
* Reclaiming infrastructure resources

Expired labs are identified using due dates and expiration thresholds.

## API Documentation

Swagger/OpenAPI documentation is automatically generated.

The API includes modules for:

* Authentication
* Users
* Courses
* Lab Templates
* VM Templates
* Lab Instances
* VM Instances
* Administration

Interactive API testing is available through Swagger UI.

## Key Design Goals

* Browser-based cybersecurity labs
* Automated VM provisioning
* Secure multi-user architecture
* Role-based access control
* Isolated lab networking
* Proxmox integration
* Scalable virtual lab deployment
* Minimal instructor overhead

The backend serves as the orchestration layer that transforms uploaded VM templates and reusable lab content into individualized, network-isolated virtual lab environments for students.
