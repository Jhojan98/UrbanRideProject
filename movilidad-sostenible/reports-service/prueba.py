import requests

url = 'http://34.9.26.232:8090/report/api/admin/overview.pdf'
params = {'lang': 'es'}
headers = {'accept': 'application/json'}

response = requests.get(url, params=params, headers=headers)

if response.status_code == 200:
    with open('admin-overview.pdf', 'wb') as file:
        file.write(response.content)
    print("✓ PDF descargado: admin-overview.pdf")
else:
    print(f"✗ Error: {response.status_code}")