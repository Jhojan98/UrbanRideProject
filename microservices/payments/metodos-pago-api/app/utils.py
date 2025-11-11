import re

def luhn_valid(numero: str) -> bool:
    if not numero:
        return False
    num = re.sub(r"[\s-]", "", numero)
    if not num.isdigit() or not (13 <= len(num) <= 19):
        return False
    total = 0
    alt = False
    for ch in reversed(num):
        d = ord(ch) - 48
        if alt:
            d *= 2
            if d > 9:
                d = (d % 10) + 1
        total += d
        alt = not alt
    return total % 10 == 0

def detectar_marca(numero: str) -> str:
    if not numero:
        return "DESCONOCIDA"
    n = re.sub(r"\D", "", numero)
    if n.startswith("4"):
        return "VISA"
    if len(n) >= 2:
        two = n[:2]
        if two in {"34", "37"}:
            return "AMEX"
        if two in {"36", "38", "30"}:
            return "DINERS"
        if two == "35":
            return "JCB"
        try:
            two_i = int(two)
            if 51 <= two_i <= 55:
                return "MASTERCARD"
        except ValueError:
            pass
    return "DESCONOCIDA"

def mask(numero: str) -> str:
    if not numero:
        return ""
    n = re.sub(r"\D", "", numero)
    if len(n) >= 4:
        return f"**** **** **** {n[-4:]}"
    return numero
