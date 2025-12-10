export default {
    loading: 'Loading...',
    header: { welcome_user: 'Welcome back, {name}' },
    tabs: {
        overview: 'Balance and Plans',
        trips: 'Trips',
        fines: 'Fines'
    },
    trips: {
        title: 'Trip History',
        startStation: 'Start Station',
        endStation: 'End Station',
        route: 'Route',
        date: 'Date',
        duration: 'Duration',
        cost: 'Cost',
        status: 'Status',
        viewAll: 'View all trips',
        noTrips: 'No trips recorded'
    },
    fines: {
        title: 'My Fines',
        fineId: 'Fine ID',
        reason: 'Reason',
        amount: 'Amount',
        date: 'Date',
        description: 'Description',
        status: 'Status',
        paid: 'Paid',
        pending: 'Pending',
        payNow: 'Pay Now',
        noFines: 'No pending fines',
        noAmount: 'Cannot process payment without amount',
        noAmountError: 'Error: Cannot process payment without amount',
        notIdentified: 'Error: User not identified',
        insufficientBalance: 'Insufficient balance. Your current balance is {balance} and the fine is {fine}',
        confirmPayment: 'Do you want to pay fine #{id} for {amount}?\n\nCurrent balance: {balance}\nBalance after payment: {remaining}',
        paymentSuccess: 'Fine paid successfully! Your balance has been updated.',
        paymentError: 'Error processing fine payment'
    },
    loyalty: {
        title: 'Loyalty Points',
        pointsLabel: 'Current points',
        missingPoints: '{points} points left for next level.',
        rewardsTitle: 'Your rewards',
        reward1: '1 hour free ride',
        reward2: '10% subscription discount',
        reward3: 'Access to premium bikes',
        redeem: 'Redeem points'
    },
    balance: {
        title: 'Balance / Registered Card',
        currentBalance: 'Current balance',
        registeredCard: 'Registered Card',
        expires: 'Expires:',
        managePayments: 'Manage payment methods',
        addBalance: 'Add balance',
        currency: 'Currency'
    }
};
