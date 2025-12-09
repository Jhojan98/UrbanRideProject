export default {
    title: 'Payment Methods',
    subtitle: 'Manage your payment methods for reservations',
    primary: 'Primary',
    makePrimary: 'Make Primary',
    delete: 'Delete',
    addNewTitle: 'Add new payment method',
    cardType: 'Card type',
    selectType: 'Select type',
    cardNumber: 'Card number',
    expiry: 'Expiration date',
    cvv: 'CVV',
    setAsPrimary: 'Set as primary payment method',
    adding: 'Adding',
    addCard: 'Add Card',
    confirmDelete: 'Are you sure you want to delete this payment method?',
    visa: 'Visa',
    mastercard: 'Mastercard',
    amex: 'American Express',
    recharge: {
        title: 'Reload Balance',
        subtitle: 'Choose the amount you want to reload to your account',
        selectAmount: 'Please select an amount',
        processing: 'Processing',
        rechargeBtn: 'Reload Balance',
        security: '🔒 You will be redirected to Stripe to complete the payment securely',
        notAuthenticated: 'No authenticated user found. Please login again',
        priceNotFound: 'PriceId not configured for the selected amount',
        error: 'Error starting payment',
        tryAgain: 'Please try again',
        currency: 'Currency',
        estimatedNote: 'Note: Amounts are shown in {currency} for your reference. Payment is processed in USD'
    },
    success: {
        title: 'Payment completed ✅',
        subtitle: 'Thank you for your purchase! Your balance has been reloaded successfully',
        sessionId: 'Session ID:',
        user: 'User:',
        updatedBalance: 'Updated balance:',
        viewBalance: 'View my updated balance',
        backHome: 'Back to home'
    },
    cancel: {
        title: 'Payment cancelled ❌',
        subtitle: 'The payment was cancelled or not completed. If it was a mistake, try again',
        backHome: 'Back to home'
    }
};
