// Asegurar que la URL base esté disponible, con fallback
const provider = process.env.VUE_APP_EXCHANGE_RATE_API_KEY || 'https://api.exchangerate-api.com/v4/latest';

async function fetchExchangeRate(fromCurrency: string, toCurrency: string, mount: number): Promise<number> {
  const url = `${provider}/${fromCurrency}`;
  console.log('Fetching exchange rate from:', url);
  console.log('VUE_APP_EXCHANGE_RATE_API_KEY:', process.env.VUE_APP_EXCHANGE_RATE_API_KEY);
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Error fetching exchange rate: ${response.statusText}`);
  }

  const data = await response.json();
  const rate = data.rates[toCurrency];
  if (!rate) {
    throw new Error(`Exchange rate not found for currency: ${toCurrency}`);
  }

  return mount * rate;
}

export { fetchExchangeRate };


