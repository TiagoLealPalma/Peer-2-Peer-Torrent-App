# Peer-to-Peer Torrent App

A decentralized file-sharing system built in Java, inspired by BitTorrent. This application allows peers to share and download files directly from each other without relying on centralized servers or trackers. It supports dynamic peer discovery, chunked file distribution, and seamless NAT traversal for a fully decentralized experience.

---

## 🧠 Architecture Overview

This project follows a modular, peer-to-peer architecture:

- Peers act as both clients and servers, able to upload and download simultaneously.
- Files are split into chunks and shared in parallel across the network.
- Each peer maintains a list of known peers and exchanges it with new connections to expand the network.
- Network communication is handled via TCP sockets, with threading to support concurrent uploads/downloads.

---

## 🚀 Key Features

- **Decentralized File Distribution**  
  Efficient chunk-based file sharing allows pieces to be downloaded from multiple peers at once, reducing overall download times.

- **Recursive Peer Discovery**  
  Peers automatically discover additional peers through existing connections.  
  Example:  
  - Peer A is connected to Peer C  
  - Peer B connects to Peer A  
  - Peer B learns about Peer C and accesses C's shared files  
  This forms a **recursive peer propagation** mechanism that ensures distributed file availability across the network.

- **NAT Traversal with UPnP**  
  Automatically opens required ports on routers using **Universal Plug and Play (UPnP)**. This allows peers behind NAT to accept incoming connections without manual configuration.

- **Peer List Propagation**  
  Every peer shares its list of known peers when connecting, ensuring the network adapts to changes and remains connected.

- **Cross-Platform Java Implementation**  
  Written entirely in Java for portability, ease of debugging, and future extensibility.

---

## 🛠️ Getting Started

### Prerequisites

- Java 11 or higher
- An IDE like IntelliJ or Eclipse (recommended for running and debugging)

### Clone and Run

```bash
git clone https://github.com/TiagoLealPalma/Peer-2-Peer-Torrent-App.git
cd Peer-2-Peer-Torrent-App
