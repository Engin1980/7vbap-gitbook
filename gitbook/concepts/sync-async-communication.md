# Sync/Async Communication

## Synchronous and Asynchronous Programming

Modern software systems are rarely composed of a single executable unit. A typical application consists of multiple components that need to exchange information and cooperate to accomplish a task. These components may be located within the same process, in different processes on the same machine, or on entirely different servers. Regardless of their physical location, a fundamental design decision concerns how one component interacts with another: synchronously or asynchronously.

In **synchronous programming**, the caller initiates an operation and waits until it has been completed. During this waiting period, the caller cannot continue with any logic that depends on the result. The execution flow therefore follows a straightforward sequence: request, wait, receive a response, continue.

Consider a simple example in which an application requests information about a customer:

```
getCustomer(123)
↓
wait for result
↓
process customer data
```

The key characteristic is that the caller remains blocked until the requested operation finishes. This model is intuitive because program execution follows a direct and predictable path. Developers can read the code from top to bottom and easily understand the order of operations.

Synchronous execution works well when operations are fast and results are immediately required. For example, if a user enters login credentials, the application cannot proceed until authentication has been completed. Waiting is unavoidable because the next step depends on the outcome of the previous one.

However, many operations in real systems are relatively slow. Accessing a database, communicating over a network, reading large files, or contacting external systems may require milliseconds or even seconds. While waiting for such operations, the processor often performs no useful work. As the number of users grows, a large portion of the system's resources may be occupied merely waiting for responses.

This limitation motivates the use of **asynchronous programming**.

In asynchronous programming, a component initiates an operation but does not wait for its completion. Instead, it continues performing other tasks. The result is delivered later, either through a callback, an event, a message, or another notification mechanism.

Conceptually, the execution flow changes from:

```
request
↓
wait
↓
continue
```

to:

```
request
↓
continue working
↓
receive result later
```

Imagine an online store generating a sales report. Creating the report might take several minutes because it involves processing a large volume of data. From the user's perspective, it is unnecessary to keep the browser waiting the entire time. Instead, the system can acknowledge the request immediately and generate the report in the background. Once the report is finished, the user can be notified that it is available for download.

The primary benefit of asynchronous execution is improved resource utilization. While one operation is waiting for an external resource, the application can continue processing other tasks. This enables a system to serve more users with the same computational resources and generally improves scalability.

The distinction between synchronous and asynchronous behavior can be understood through a real-world analogy. Imagine a customer visiting a restaurant. In a synchronous scenario, the customer places an order and remains standing at the counter until the meal is prepared. Only then can the customer continue. In an asynchronous scenario, the customer places the order, receives a number, sits down, and continues with other activities until the meal is ready. The work still takes the same amount of time, but the waiting period is utilized more effectively.

It is important to understand that asynchronous programming does not necessarily make an operation faster. A database query that requires five seconds will still require approximately five seconds. The difference is that the caller does not spend those five seconds blocked and idle. Instead, the system can perform useful work while waiting for the result.

For this reason, modern distributed systems often combine both approaches. Synchronous communication is commonly used when an immediate result is required, whereas asynchronous communication is preferred for long-running tasks, background processing, notifications, and other operations where immediate feedback is not essential.

The choice between synchronous and asynchronous communication is therefore not merely a technical implementation detail. It affects system performance, scalability, responsiveness, fault tolerance, and the overall architecture of software systems. Understanding this distinction is a prerequisite for studying communication between front-end and back-end components, as well as interactions among services in distributed and microservice-based architectures.

## Synchronous Communication Techniques

In distributed systems, synchronous communication follows a request-response model. One component initiates communication and waits for the other component to process the request and return a result. Although the implementation details differ, all synchronous communication technologies share this fundamental characteristic: the caller cannot proceed with operations that depend on the result until a response has been received.

### HTTP

**Hypertext Transfer Protocol (HTTP)** is the most widely used communication protocol in modern software systems. It operates according to a request-response model, where a client sends a request to a server and waits for a response.

A typical interaction consists of four steps. The client establishes a connection, sends a request, the server processes the request, and a response is returned.

```
Client                         Server
  | ---- HTTP Request ------> |
  |                            |
  | <--- HTTP Response ------ |
```

Although originally developed for web pages, HTTP is now commonly used for machine-to-machine communication, especially in web APIs and distributed systems.

A request contains a method, a resource identifier, headers, and optionally a body:

```
GET /customers/123 HTTP/1.1Host: api.example.com
```

The server returns a response:

```
HTTP/1.1 200 OK{  "id": 123,  "name": "John Smith"}
```

The popularity of HTTP stems from its simplicity, widespread support, interoperability, and compatibility with network infrastructure. Its main disadvantage is the relatively large protocol overhead compared to lower-level approaches.

### TCP

**Transmission Control Protocol (TCP)** is a transport-layer protocol that provides reliable communication between two endpoints. Many higher-level protocols, including HTTP, operate on top of TCP.

TCP guarantees that:

* data arrives without loss,
* data arrives in the correct order,
* duplicate packets are eliminated.

Communication begins with connection establishment, often called the _three-way handshake_.

```
Client                Server
  | ---- SYN -------> |
  | <--- SYN-ACK --- |
  | ---- ACK -------> |
```

Once established, both sides can exchange byte streams reliably.

Unlike HTTP, TCP does not define message formats, commands, resources, or data structures. It merely provides a reliable communication channel. Developers must define their own protocol on top of TCP.

TCP is therefore more flexible and efficient than HTTP, but also more complex to implement and maintain.

### Sockets

A **socket** is a software abstraction representing one endpoint of a network connection. It provides an interface through which applications access networking services offered by the operating system.

It is important to distinguish between TCP and sockets. TCP is a communication protocol, whereas a socket is an API used to access that protocol.

A TCP socket connection typically looks as follows:

```
Application
      |
    Socket
      |
     TCP
      |
      IP
```

Using sockets, applications can exchange arbitrary data:

```java
Socket socket =
        new Socket("server.example.com", 5000);

OutputStream out = socket.getOutputStream();
out.write("Hello".getBytes());
```

Sockets offer maximum flexibility and low overhead but require developers to implement message framing, serialization, error handling, authentication, and protocol design manually.

### WebSockets

WebSockets were introduced to overcome the request-response limitations of HTTP. Traditional HTTP communication requires the client to initiate every interaction. The server cannot independently send information to the client.

WebSockets establish a persistent connection that remains open after the initial handshake.

```
Client <=================> Server
        Bidirectional
          Channel
```

After the connection is established, either side can send messages at any time.

Examples include:

* live chat systems,
* online games,
* financial trading platforms,
* real-time dashboards,
* collaborative editing applications.

Although WebSockets allow asynchronous data exchange after connection establishment, the establishment of the connection itself is typically performed synchronously. From an architectural perspective, they are often treated as a persistent synchronous communication channel because both endpoints remain directly connected.

### SOAP

**Simple Object Access Protocol (SOAP)** is a protocol for exchanging structured information between distributed applications.

SOAP messages are represented using XML and follow a strict format.

Example request:

```xml
<soap:Envelope>
  <soap:Body>
    <GetCustomerRequest>
      <id>123</id>
    </GetCustomerRequest>
  </soap:Body>
</soap:Envelope>
```

SOAP was designed for enterprise integration and provides standardized support for:

* security,
* transactions,
* reliability,
* message validation.

Because of its extensive standards and XML-based format, SOAP messages are typically larger and more complex than modern alternatives.

Despite declining popularity in web applications, SOAP remains common in banking systems, government services, healthcare platforms, and large enterprise integrations.

### Remote Procedure Call (RPC)

The idea behind **Remote Procedure Call (RPC)** is to make a remote operation appear like a local function call.

Instead of constructing network messages manually, the developer invokes a method:

```java
Customer customer =
        customerService.getCustomer(123);
```

Behind the scenes, the framework:

1. serializes parameters,
2. sends a network request,
3. executes the remote procedure,
4. receives the result,
5. returns the value to the caller.

Conceptually:

```
Local Call

result = getCustomer(123)

↓

Remote Communication

serialize
send request
execute remotely
receive response
deserialize
```

RPC improves developer productivity because distributed communication resembles ordinary object-oriented programming.

However, developers must remember that a remote call is fundamentally different from an in-process function call. Network latency, failures, timeouts, and service unavailability remain significant concerns.

### gRPC and Protocol Buffers

**gRPC** is a modern RPC framework that implements the Remote Procedure Call concept in an efficient and language-independent manner.

Instead of manually defining request formats, services are described using **Protocol Buffers (Protobuf)**.

A service definition may look as follows:

```java
service CustomerService {
  rpc GetCustomer(CustomerRequest)
      returns (CustomerResponse);
}
```

Message structures are also formally defined:

```json
message CustomerRequest {
  int32 id = 1;
  string title = 2;
}
```

From this description, code generators can automatically produce client and server classes for multiple programming languages.

Compared with traditional text-based formats, Protocol Buffers use compact binary encoding, resulting in:

* smaller messages,
* lower bandwidth consumption,
* faster serialization,
* faster deserialization.

A typical gRPC interaction follows this sequence:

```
Client
   |
   | GetCustomer(123)
   v
gRPC Runtime
   |
   | Binary Protocol Buffer Message
   v
Server
   |
   | Execute Service
   v
Response
```

Because of its performance characteristics and strong typing, gRPC has become particularly popular in microservice architectures where a large number of internal service-to-service calls occur.

### Comparison

The discussed synchronous communication techniques operate at different abstraction levels.

| Technique  | Abstraction Level                 | Message Format   | Main Strength                         |
| ---------- | --------------------------------- | ---------------- | ------------------------------------- |
| TCP        | Transport protocol                | Raw bytes        | Reliability and efficiency            |
| Sockets    | Programming interface             | Arbitrary        | Maximum flexibility                   |
| HTTP       | Application protocol              | Text or binary   | Simplicity and interoperability       |
| WebSockets | Persistent communication protocol | Messages         | Real-time bidirectional communication |
| SOAP       | Service protocol                  | XML              | Enterprise-level standardization      |
| RPC        | Programming model                 | Various          | Transparency of remote calls          |
| gRPC       | RPC framework                     | Protocol Buffers | High performance and strong typing    |

These technologies should not be viewed as direct competitors in all situations. Rather, they occupy different layers of the communication stack and address different requirements. For example, HTTP, SOAP, and gRPC typically use TCP underneath, while sockets provide the programming interface through which TCP communication is often implemented. RPC and gRPC focus on simplifying distributed programming, whereas WebSockets are specifically designed for long-lived bidirectional communication.

## Asynchronous Communication Techniques

Asynchronous communication is based on a different interaction model than traditional request-response communication. Instead of waiting for an immediate answer, a component sends a message and continues its execution. The receiving component processes the message independently, often at a later time.

This approach reduces coupling between components. The sender does not need to know whether the receiver is currently available, how long processing will take, or even how many consumers will eventually receive the message.

A typical asynchronous interaction can be represented as:

```
Producer
    |
    | Message
    v
 Message Broker
    |
    +----> Consumer A
    |
    +----> Consumer B
    |
    +----> Consumer C
```

The central element of most asynchronous architectures is a **message broker**, which acts as an intermediary between producers and consumers.

### ActiveMQ

**ActiveMQ** is one of the oldest and most established message-oriented middleware systems. It was originally designed to implement enterprise messaging patterns and became popular in Java enterprise systems.

The core idea is straightforward. Applications send messages to the broker, and the broker ensures their delivery to the appropriate consumers.

In its simplest form:

```
Sender
   |
   v
ActiveMQ
   |
   v
Receiver
```

ActiveMQ supports both **queues** and **topics**.

A queue implements a point-to-point communication model. Multiple consumers may listen to the queue, but each message is processed by only one consumer.

```
Order Queue
    |
    +---- Worker 1
    |
    +---- Worker 2
    |
    +---- Worker 3

Message processed once
```

A topic implements a publish-subscribe model. Every subscriber receives a copy of the message.

```
Order Created Topic
       |
       +---- Email Service
       |
       +---- Analytics Service
       |
       +---- Billing Service
```

ActiveMQ is particularly suitable for enterprise integration scenarios where reliability, transaction support, and compatibility with existing systems are important.

### RabbitMQ

**RabbitMQ** is one of the most widely used message brokers in distributed systems. Conceptually, it is similar to ActiveMQ but emphasizes flexible message routing and ease of deployment.

The architecture introduces an intermediate component called an **exchange**.

```
Producer
    |
    v
 Exchange
    |
    +---- Queue A
    |
    +---- Queue B
    |
    +---- Queue C
```

The exchange determines how messages are routed to queues.

For example:

```
Order.Created
    |
    +---- Email Queue
    |
    +---- Invoice Queue
```

When a new order is created, multiple downstream services can react independently.

RabbitMQ is often selected for business applications because it offers:

* reliable delivery,
* acknowledgements,
* message persistence,
* dead-letter queues,
* flexible routing mechanisms.

A particularly important characteristic is that RabbitMQ focuses primarily on **message delivery**, not long-term storage. Once messages are successfully consumed, they are usually removed from the broker.

### ZeroMQ

**ZeroMQ (ØMQ)** differs significantly from ActiveMQ and RabbitMQ.

Strictly speaking, ZeroMQ is not a message broker at all. It is a high-performance messaging library that enables applications to exchange messages directly.

Traditional broker-based communication:

```
Producer
    |
    v
Broker
    |
    v
Consumer
```

ZeroMQ communication:

```
Producer <-------> Consumer
```

No central broker is required.

The library provides multiple communication patterns, including:

* request-reply,
* publish-subscribe,
* pipeline,
* push-pull.

Because there is no broker, ZeroMQ generally offers lower latency and higher throughput than broker-based solutions.

A typical use case is communication between high-performance components where minimizing communication overhead is essential, such as:

* scientific computing,
* real-time analytics,
* distributed simulations,
* financial systems.

The trade-off is greater responsibility for developers, who must implement many services typically provided by a message broker.

### Apache Kafka

**Apache Kafka** was developed to solve a different class of problems than traditional message brokers.

While systems such as RabbitMQ focus on reliable message delivery, Kafka focuses on large-scale event streaming and durable event storage.

The fundamental abstraction in Kafka is the **event log**.

Instead of removing messages after delivery, Kafka stores them for a configurable retention period.

```
Producer
    |
    v
 Topic
 ----------------------------------
 | Event 1                        |
 | Event 2                        |
 | Event 3                        |
 | Event 4                        |
 ----------------------------------
```

Consumers maintain their own position within the log.

```
Consumer A -> reading Event 3
Consumer B -> reading Event 1
Consumer C -> reading Event 4
```

This allows several consumers to read the same events independently.

Unlike traditional queues, Kafka treats events as a persistent stream of historical records. This enables capabilities such as:

* replaying events,
* rebuilding system state,
* auditing,
* event sourcing,
* stream processing.

For example, if an analytics service fails for several hours, it can later resume consumption from the stored event history.

Kafka therefore serves simultaneously as:

1. a messaging system,
2. an event store,
3. a streaming platform.

This architecture has made Kafka extremely popular in large-scale microservice environments and big data ecosystems.

### Comparison of Messaging Models

Although all four technologies support asynchronous communication, they solve slightly different problems.

| Technology | Central Broker | Persistent Storage | Primary Goal                      |
| ---------- | -------------- | ------------------ | --------------------------------- |
| ActiveMQ   | Yes            | Optional           | Enterprise messaging              |
| RabbitMQ   | Yes            | Optional           | Reliable message delivery         |
| ZeroMQ     | No             | No                 | High-performance messaging        |
| Kafka      | Yes            | Core feature       | Event streaming and event storage |

The key distinction lies in message lifetime. ActiveMQ and RabbitMQ usually treat messages as temporary communication artifacts whose purpose is delivery. Kafka treats events as durable records that may have value long after their initial processing.

### Redis as a Messaging Platform

Although **Redis** was originally designed as an in-memory key-value database, it can also be used for asynchronous communication.

The simplest mechanism is **Pub/Sub (Publish/Subscribe)**.

```
Publisher
     |
     v
 Redis Channel
     |
     +---- Subscriber A
     |
     +---- Subscriber B
```

When a message is published, all active subscribers receive it.

However, Redis Pub/Sub has an important limitation: if a subscriber is disconnected when a message is published, the message is lost.

```
Publisher
     |
     v
 Message

Subscriber offline
       ↓
Message lost
```

For more reliable messaging, Redis provides **Streams**, introduced in Redis 5.

Streams resemble a simplified event log:

```
Stream
--------------------------------
1: UserCreated
2: OrderCreated
3: PaymentApproved
--------------------------------
```

Consumers can track their reading position and continue after failures.

This makes Redis Streams conceptually closer to Kafka than to traditional Pub/Sub systems, although with fewer features and scalability options.

Redis-based messaging is often attractive because many applications already use Redis for caching. Small and medium-sized systems can therefore implement asynchronous communication without introducing another infrastructure component.

Nevertheless, Redis is usually not the first choice for large-scale event streaming. When requirements include very high throughput, long-term event retention, event replay, or distributed stream processing, dedicated platforms such as Kafka typically provide a more appropriate architecture.

### Choosing an Asynchronous Technology

The choice depends primarily on the communication requirements rather than on raw performance.

If the goal is reliable business message delivery between services, RabbitMQ and ActiveMQ are common choices. If the objective is durable event storage and large-scale event streaming, Kafka is generally more suitable. When maximum throughput and minimum latency are required inside a controlled environment, ZeroMQ can be effective. Redis offers a lightweight alternative when message volumes are moderate and the infrastructure already depends on Redis for caching or session management.

From an architectural perspective, RabbitMQ and ActiveMQ are often viewed as **message brokers**, Kafka as an **event streaming platform**, ZeroMQ as a **messaging library**, and Redis as an **in-memory data platform** that also provides messaging capabilities. These distinctions become increasingly important when designing scalable microservice-based systems.
