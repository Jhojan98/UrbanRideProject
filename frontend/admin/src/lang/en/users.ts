export default {
    title: 'Users Dashboard',
    nav: {
        users: 'Users',
        fines: 'Fines',
        cac: 'Complaints & Comments',
        travel: 'Trips'
    },
    list: {
        title: 'Users List',
        searchPlaceholder: 'Search user by name or email...',
        tableHeaders: {
            user: 'User',
            email: 'Email',
            subscription: 'Subscription Type',
            balance: 'Balance',
            registrationDate: 'Registration Date',
            actions: 'Actions'
        },
        noSubscription: 'No subscription',
        btnView: 'View details',
        noData: 'No users found'
    },
    fines: {
        title: 'Fines Management',
        filterAll: 'All statuses',
        filterPending: 'Pending',
        filterPaid: 'Paid',
        filterCancelled: 'Cancelled',
        stats: {
            total: 'Total Fines',
            pending: 'Pending',
            paid: 'Paid',
            totalAmount: 'Total Amount'
        },
        tableHeaders: {
            id: 'ID',
            reason: 'Reason',
            description: 'Description',
            status: 'Status',
            amount: 'Amount',
            date: 'Date'
        },
        noData: 'No fines found'
    },
    cac: {
        title: 'Complaints & Comments',
        filterAll: 'All statuses',
        filterPending: 'Pending',
        filterInProcess: 'In Progress',
        filterResolved: 'Resolved',
        filterClosed: 'Closed',
        stats: {
            total: 'Total',
            pending: 'Pending',
            inProcess: 'In Progress',
            resolved: 'Resolved'
        },
        ticketId: 'Ticket',
        travelId: 'Trip',
        noData: 'No complaints or comments found',
        modalTitle: 'Ticket Details',
        modalSections: {
            status: 'Status',
            description: 'Description',
            travelInfo: 'Trip Information',
            travelIdLabel: 'Trip ID',
            date: 'Date'
        }
    },
    travel: {
        title: 'Trips',
        btnRefresh: 'Refresh',
        loading: 'Loading...',
        tableHeaders: {
            id: 'ID',
            startStation: 'Start Station',
            endStation: 'End Station',
            start: 'Start',
            end: 'End',
            status: 'Status',
            type: 'Type'
        },
        noData: 'No trips recorded.'
    },
    modal: {
        title: 'User Information',
        tabs: {
            travels: 'Trips',
            fines: 'Fines',
            cacs: 'Complaints/Comments'
        },
        loading: 'Loading information...',
        noTravels: 'This user has no trips recorded',
        noFines: 'This user has no fines',
        noCacs: 'This user has no complaints or comments',
        travelCard: {
            id: 'Trip',
            type: 'Type',
            bicycle: 'Bicycle',
            startStation: 'Start Station',
            endStation: 'End Station',
            start: 'Start',
            end: 'End'
        },
        fineCard: {
            id: 'Fine',
            reason: 'Reason',
            amount: 'Amount',
            date: 'Date'
        },
        cacCard: {
            id: 'Ticket',
            description: 'Description',
            travel: 'Trip',
            date: 'Date'
        }
    }
}
