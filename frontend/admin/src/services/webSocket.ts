let socket: WebSocket | null = null

export function createWebSocket() {
    // Fallback para asegurar tipo string
    const wsUrl = process.env.VUE_APP_WEBSOCKET_BICYCLES_URL || 'http://localhost:8002';
    socket = new WebSocket(wsUrl)

    socket.onopen = () => {
        console.log('WebSocket connection established')
    }
    socket.onclose = () => {
        console.log('WebSocket connection closed')
    }

    socket.onerror = (error) => {
        console.error('WebSocket error:', error)
    }
    return socket
}

export function sendWS(message: string) {
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(message)
    } else {
        console.error('WebSocket is not open. Ready state:', socket ? socket.readyState : 'No socket')
    }
}
