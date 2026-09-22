# Common Backend Techniques and Technologies

TODO IMG

## Infractructure

### Kubernetes

Kubernetes is an open-source container orchestration platform used to automate the deployment, scaling, and management of containerized applications. Instead of running individual containers manually, Kubernetes groups them into logical units called pods and manages their lifecycle across a cluster of machines. It provides features such as automatic scaling, load balancing, self-healing, rolling updates, and service discovery. Kubernetes has become the de facto standard for running modern cloud-native applications because it simplifies the management of large distributed systems.

### Private Cloud

A private cloud is a cloud computing environment that is dedicated to a single organization. The infrastructure may be hosted on-premises within the organization's own data center or by a third-party provider, but the resources are not shared with other customers. Private clouds offer greater control over security, compliance, networking, and resource allocation. They are commonly used by organizations that must meet strict regulatory requirements or handle sensitive data.

### Public Cloud

A public cloud is a cloud computing environment where computing resources are provided by a third-party cloud vendor and shared among multiple customers. Services such as virtual machines, storage, databases, and networking are delivered over the Internet and can typically be provisioned on demand. Major public cloud providers include Microsoft Azure, Amazon Web Services (AWS), and Google Cloud Platform (GCP). Public clouds offer high scalability, global availability, and a pay-as-you-go pricing model, reducing the need for organizations to maintain their own infrastructure.

### Virtual Machine (VM)

A virtual machine (VM) is a software-based emulation of a physical computer. Each virtual machine contains its own operating system, virtual hardware, applications, and configuration, making it function as an independent computing environment. Virtual machines are managed by a hypervisor, which allocates physical resources such as CPU, memory, and storage among multiple VMs running on the same host. Virtualization improves hardware utilization, simplifies resource management, and enables the isolation of workloads.

### Bare Metal

Bare metal refers to a physical server running directly on hardware without an intermediate virtualization layer. Applications and operating systems execute directly on the machine, allowing full access to the available hardware resources. Bare metal deployments often provide the highest possible performance and lowest latency because there is no overhead introduced by a hypervisor. They are commonly used for high-performance computing, large databases, real-time systems, and other workloads where maximum efficiency and predictable performance are critical requirements.

## Backend (REST API)

### KubeSphere System Components

KubeSphere is a cloud-native platform built on top of Kubernetes that provides a unified management layer for containerized applications. It extends Kubernetes with additional services that simplify cluster administration, application deployment, monitoring, security, and DevOps automation.

The **API Server** acts as the central entry point for communication within the platform. It exposes APIs that allow users, web interfaces, and other system components to interact with the cluster. Every operation, such as creating a deployment, managing users, or retrieving cluster information, is processed through the API server.

The **API Gateway** serves as a routing layer between clients and backend services. Instead of exposing every internal service directly, the gateway provides a single access point and forwards requests to the appropriate destination. This approach improves security, simplifies networking, and enables centralized authentication and access control.

The **Controller Manager** continuously monitors the cluster state and ensures that the actual state matches the desired configuration. For example, if a deployment should contain three application instances and one instance fails, the controller manager automatically starts a replacement instance to restore the desired state.

### Monitoring

Monitoring provides visibility into the health, performance, and resource consumption of applications and infrastructure. In modern cloud-native environments, monitoring is essential for detecting problems before they impact users.

**Metrics Server** is a lightweight monitoring component that collects basic resource usage data from cluster nodes and pods. It provides information such as CPU and memory utilization and is commonly used by Kubernetes autoscaling mechanisms.

**Prometheus** is a more advanced monitoring solution designed for time-series data collection and analysis. It periodically gathers metrics from applications, services, and infrastructure components, stores them in a specialized database, and allows powerful querying. Prometheus is often combined with visualization tools such as Grafana to create dashboards and alerts.

### Logging

Logging is the process of collecting and storing information generated by applications and infrastructure components during execution. Log entries record events such as application startup, configuration changes, errors, warnings, and user actions.

Centralized logging allows administrators and developers to analyze system behavior across multiple services. In microservice environments, logs are often aggregated into systems such as Elasticsearch, OpenSearch, or Loki, where they can be searched and analyzed efficiently. Effective logging significantly reduces troubleshooting time and improves system observability.

### DevOps

DevOps represents a set of practices that integrate software development and IT operations. The primary goal is to automate software delivery processes and improve collaboration between development and operational teams.

**Jenkins** is one of the most widely used automation servers. It executes build pipelines that compile source code, perform tests, package applications, and deploy them to different environments. Jenkins enables Continuous Integration (CI) and Continuous Delivery (CD) practices.

**SonarQube** is a platform for continuous code quality inspection. It analyzes source code to identify bugs, security vulnerabilities, code smells, duplicated code, and maintainability issues. By integrating SonarQube into build pipelines, teams can enforce quality standards automatically.

**Source-to-Image (S2I)** is a build process that creates container images directly from application source code. The mechanism combines source code with predefined builder images and produces runnable container images without requiring developers to write complex Dockerfiles.

### Microservices

A microservice is a small, independently deployable software component that implements a specific business capability. Unlike monolithic applications, where all functionality is contained within a single deployment unit, microservice architectures split the system into multiple specialized services.

Each microservice can be developed, deployed, scaled, and maintained independently. Services typically communicate through REST APIs, gRPC, messaging systems, or event-driven architectures. This approach improves scalability and flexibility but introduces additional complexity in communication, monitoring, deployment, and fault handling.

### Notification Services

Notification services are responsible for delivering information to users or external systems. Notifications can be sent through email, SMS messages, mobile push notifications, chat platforms, or internal messaging systems.

In enterprise applications, notification services are often implemented as dedicated microservices. They receive events from other services and determine how and where notifications should be delivered. Decoupling notifications from business logic improves maintainability and scalability.

### Databases and Cache

Databases provide persistent storage for application data. They ensure that information remains available even when applications restart or servers fail. Common database systems include PostgreSQL, MySQL, SQL Server, and MongoDB.

A **cache** is a temporary high-speed storage layer used to reduce the need for repeated database access. Frequently requested data is stored in memory, allowing applications to retrieve it significantly faster.

**Redis** is one of the most popular in-memory caching systems. It stores data in RAM and offers extremely low latency. Besides caching, Redis can be used for session management, distributed locking, message queues, rate limiting, and real-time analytics. Because memory access is much faster than disk access, Redis can substantially improve application performance and reduce database load.

### Security

Security protects applications, infrastructure, and data from unauthorized access and malicious activities.

**LDAP (Lightweight Directory Access Protocol)** is a protocol used to access and manage centralized user directories. Organizations often use LDAP servers to store user accounts, groups, and authentication information. Applications can integrate with LDAP to provide centralized user management and single sign-on capabilities.

**RBAC (Role-Based Access Control)** is an authorization model in which permissions are assigned to roles rather than directly to individual users. Users receive access rights by being assigned to one or more roles. RBAC simplifies security administration and ensures consistent access control policies throughout the system.

### Storage

Storage refers to the mechanisms used to persist data generated by applications. Containerized applications often require persistent storage because container file systems are typically temporary.

Kubernetes supports persistent storage through Persistent Volumes (PV) and Persistent Volume Claims (PVC). Storage solutions may be based on local disks, network file systems, distributed storage platforms, or cloud storage services. Reliable storage is essential for databases, file repositories, backups, and application state preservation.

### Network

Networking enables communication between applications, services, users, and infrastructure components. In Kubernetes environments, networking is responsible for connecting pods, exposing services, and routing traffic.

Key networking concepts include service discovery, load balancing, ingress controllers, DNS resolution, and network policies. Service discovery allows applications to find each other dynamically, while load balancing distributes traffic across multiple service instances. Network policies provide security by controlling which services are allowed to communicate with each other.

A properly designed network architecture ensures reliable communication, security, scalability, and high availability of applications running in modern cloud-native environments.



## Observability

Here we deal with the performance and behavior of the system. The following approaches are used to monitor the behavior of the backend and to take appropriate action(s) if required.

### Monitoring

Monitoring is the continuous process of collecting and analyzing metrics from applications, services, and infrastructure components. Its primary purpose is to provide visibility into the current health and performance of a system.

Monitoring answers questions such as: Is the application running? How much CPU is it consuming? How many requests per second are being processed? What is the current response time? By collecting these metrics over time, administrators can identify trends, detect anomalies, and react before users experience problems.

In cloud-native environments, monitoring data is typically collected from servers, containers, databases, network devices, and application components. The collected metrics are stored as time-series data and visualized through dashboards. Alerting mechanisms can automatically notify administrators when predefined thresholds are exceeded, such as high memory consumption, low disk space, or increasing response latency.

Monitoring provides a high-level view of system behavior and serves as the first step in identifying operational problems. However, metrics alone often do not explain why a problem occurred, which is where logging and tracing become important.

### Logging

Logging is the process of recording events generated by applications and infrastructure during execution. Logs provide detailed information about what happened inside the system at a specific point in time.

A log entry typically contains a timestamp, severity level, source component, and descriptive message. Common severity levels include Information, Warning, Error, and Critical. For example, a log may indicate that a user successfully authenticated, a database query failed, or an external service became unavailable.

Unlike monitoring metrics, which are numerical and aggregated, logs contain detailed textual information. They are particularly useful when investigating incidents, debugging software defects, or auditing system activity.

In distributed systems, logs from multiple services are usually collected into a centralized logging platform. This enables developers and operators to search, filter, and correlate log entries across the entire environment. Centralized logging becomes especially important in microservice architectures, where a single user request may pass through many independent services.

### Events

Events represent significant occurrences within a system. An event describes something that happened at a specific moment and may be consumed by users, administrators, or other software components.

Examples of events include:

* A pod was started or terminated.
* A user account was created.
* An order was placed.
* A file was uploaded.
* A deployment was completed.

Unlike logs, which are primarily intended for troubleshooting and diagnostics, events often describe business or infrastructure activities that other services may react to. Event-driven architectures use this concept extensively. Instead of directly calling another service, a component publishes an event and interested systems subscribe to it.

For example, after an order is created, the ordering service might publish an "Order Created" event. Other services can independently process this event and perform tasks such as payment processing, inventory updates, or notification delivery.

Events increase decoupling between services because publishers do not need to know which consumers will process the information.

### Tracing

Tracing is the process of tracking the complete path of a request as it travels through multiple services within a distributed system.

In traditional monolithic applications, tracing is relatively simple because all processing occurs within a single application. In microservice architectures, however, a single user action may trigger communication between many services. Identifying the source of performance problems becomes significantly more difficult.

Distributed tracing solves this problem by assigning a unique trace identifier to each request. As the request moves through different services, databases, message brokers, and external APIs, all operations are linked to the same trace.

Tracing helps answer questions such as:

* Which services participated in processing the request?
* How long did each service take?
* Where did a failure occur?
* Which component introduced latency?

A trace is typically composed of multiple spans. A span represents a single operation, such as an API call or database query. By combining spans, tracing systems can reconstruct the complete request flow and visualize it as a timeline.

Popular tracing technologies include OpenTelemetry, Jaeger, and Zipkin. These tools allow developers to identify bottlenecks, diagnose failures, and optimize application performance in complex distributed environments.

### Relationship Between Monitoring, Logging, Events, and Tracing

Although these concepts are related, they serve different purposes.

Monitoring answers **"What is happening?"** by providing metrics and system health information.

Logging answers **"What exactly happened?"** by recording detailed messages generated by applications and infrastructure.

Events answer **"Which important action occurred?"** and often trigger reactions from other components.

Tracing answers **"How did this request travel through the system?"** by tracking a request across multiple services.

Together, monitoring, logging, events, and tracing form the foundation of modern system observability, allowing engineers to understand, operate, troubleshoot, and optimize complex distributed applications.
