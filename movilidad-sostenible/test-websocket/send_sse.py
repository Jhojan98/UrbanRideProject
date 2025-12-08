#!/usr/bin/env python3
"""
Script simple para enviar mensajes al endpoint /sse/test de notification-realtime-service.
Uso:
  python send_sse.py --uid testuser --msg "hola desde script"

También puedes usar curl:
  curl -X POST "http://localhost:8105/sse/test?uid=testuser&msg=hola"
"""
import argparse
import requests

parser = argparse.ArgumentParser()
parser.add_argument('--server', default='http://localhost:8105', help='URL base del servicio')
parser.add_argument('--uid', default='testuser')
parser.add_argument('--msg', default='hola desde python')
args = parser.parse_args()

url = f"{args.server.rstrip('/')}/sse/test?uid={args.uid}&msg={requests.utils.requote_uri(args.msg)}"
print('Enviando POST a:', url)
resp = requests.post(url)
print('Status:', resp.status_code)
print('Body:', resp.text)

