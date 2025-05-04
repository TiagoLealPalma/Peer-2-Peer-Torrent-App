**📦 Peer-to-Peer Torrent App**
	Peer-to-Peer Torrent App is a decentralized file-sharing system built in Java, inspired by BitTorrent. It allows users to share and download files directly between peers without relying on centralized servers or trackers. Designed for reliability and extensibility, 				this application demonstrates core P2P principles including peer discovery, distributed file availability, and NAT traversal.

**🧠 Architecture Overview**
	The application follows a modular peer-to-peer architecture:
		Peers act as both clients and servers, capable of sharing their own files and downloading from others.
		Files are split into chunks, which can be shared and requested independently to improve distribution efficiency.
		Each peer maintains a list of known peers, regularly updating and exchanging this list to discover new nodes and available files.
		Communication occurs over TCP, with logic in place to handle simultaneous requests, connections, and transfers.

**🚀 Key Features**
	Decentralized File Distribution
		Files are broken into pieces and downloaded from multiple peers in parallel, maximizing speed and availability.
	Smart Peer Relay Discovery
		Peers not only connect directly, but also learn about other peers indirectly. For example, if:
		Peer A is connected to Peer C
		Peer B connects to Peer A
		Then Peer B becomes aware of Peer C and can access shared files from C
		This effectively creates a recursive peer propagation network, expanding file availability dynamically.
	NAT Traversal with UPnP
		Uses Universal Plug and Play (UPnP) to automatically open ports on routers, allowing peers behind NATs to accept incoming connections without manual configuration.
	Dynamic Peer List Management
		Peers exchange their known-peer lists upon connecting, keeping the network connected even as nodes come and go.
	Built in Java
		Ensures platform independence and easy integration with existing Java tools or systems.
