export default {
    destination: {
        title: 'Destination Station',
        subtitle: 'Location of the station where you must leave the bike',
        confirm: 'Confirm Destination',
        address: 'Address:',
        availableSlots: 'Available slots:',
        distance: 'Approximate distance:',
        estimatedTime: 'Estimated time:'
    },
    balance: {
        currencyLabel: 'Currency',
        reload: 'Reload balance',
        currencies: {
            USD: 'Dollar',
            EUR: 'Euro',
            COP: 'Colombian Peso'
        }
    },
    report: {
        title: 'Report Problem',
        subtitle: 'Report any issue with your bike',
        bikeInfo: 'Bike information',
        bikeId: 'Bike ID',
        bikeIdPlaceholder: 'Ex: BIC-1234',
        stationWhere: 'Station where it is located',
        selectStation: 'Select station',
        problemDetails: 'Problem details',
        problemType: 'Problem type',
        severity: 'Problem severity',
        description: 'Detailed description',
        descriptionPlaceholder: 'Describe the problem with as much detail as possible',
        allowsUse: 'Does it allow bike usage?',
        noUse: 'No, unsafe to use',
        yesUse: 'Yes, but with caution',
        sending: 'Sending report',
        sendReport: 'Send Report',
        successTitle: 'Report Sent',
        successMsg: 'We received your report and are reviewing it. We will contact you if more info is needed',
        reportId: 'Report ID:',
        problems: {
            mechanical: 'Mechanical Problem',
            electrical: 'Electrical Problem',
            brakes: 'Brakes',
            tire: 'Tires',
            chain: 'Chain',
            other: 'Other'
        },
        severityLevels: {
            low: { name: 'Low', desc: 'Minor issue, does not affect use' },
            medium: { name: 'Medium', desc: 'Affects use but manageable' },
            high: { name: 'High', desc: 'Serious issue, do not use bike' }
        },
        cancelConfirm: 'Are you sure you want to cancel? Form data will be lost',
        sendError: 'Error sending report. Please try again'
    },
    footer: {
        about: 'About',
        resources: 'Resources',
        legal: 'Legal',
        copyright: '© 2026 ECORIDE'
    }
};
