<template>
    <div class="form-container">
        <img src="@/assets/ECORIDE.webp" alt="Logo" class="form-logo" />
        <h2 class="form-title">{{ $t('auth.otp.title') }}</h2>

        <!-- Mostramos el email que viene del store -->
        <div class="email-info">
            <p>{{ $t('auth.otp.codeSentTo') }} <strong>{{ email }}</strong></p>
        </div>

        <div class="verification-info">
            <p>{{ $t('auth.otp.emailSent') }}</p>
            <p>{{ $t('auth.otp.checkInbox') }}</p>
            <p>{{ $t('auth.otp.afterVerify') }}</p>
        </div>

        <button @click="checkVerification" class="form-submit">{{ $t('auth.otp.verify') }}</button>

        <div class="otp-actions">
            <button @click="resendOtp" class="btn-resend">{{ $t('auth.otp.resend') }}</button>
        </div>

        <br>
        <h4>
            <router-link class="link-inline" :to="{ name: 'login' }" @click="clearEmail">
                {{ $t('auth.otp.backToLogin') }}
            </router-link>
            <p>{{ feedback }}</p>
        </h4>
    </div>
</template>

<script setup lang="ts">
import { ref, type Ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import userAuth from '@/stores/auth';

const { t: $t } = useI18n();
const feedback: Ref<string> = ref('');
const email: Ref<string> = ref('');

const store = userAuth();
const router = useRouter();

onMounted(() => {
    // Obtenemos el email del store al cargar el componente
    if (store.tempEmail) {
        email.value = store.tempEmail;
    } else {
        feedback.value = $t('auth.otp.noEmailFound');
        setTimeout(() => {
            router.push('/login');
        }, 2000);
    }
});

const checkVerification = async () => {
    if (!email.value) {
        feedback.value = $t('auth.otp.noEmail');
        return;
    }

    // Simply redirect to login
    // Verification was already done via the link in the email
    feedback.value = $t('auth.otp.redirectToLogin');
    setTimeout(() => {
        router.push({ name: 'login', query: { verified: 'true' } });
    }, 1500);
};

const resendOtp = async () => {
    if (!email.value) {
        feedback.value = $t('auth.otp.noEmailResend');
        return;
    }

    const res = await store.generateOtp(email.value);

    if (res) {
        feedback.value = $t('auth.otp.resendSuccess');
    } else {
        feedback.value = store.message || $t('auth.otp.resendError');
    }
};

const clearEmail = () => {
    store.tempEmail = null;
    store.pendingVerification = false;
};
</script>

<style lang="scss">
@import "@/styles/forms.scss";

.otp-actions {
    margin-top: 1rem;
    text-align: center;
}

.btn-resend {
    background: transparent;
    color: var(--color-primary-light);
    border: 1px solid var(--color-primary-light);
    padding: 0.5rem 1rem;
    border-radius: 6px;
    cursor: pointer;
    font-size: 0.9rem;
    transition: all 0.3s ease;
}

.btn-resend:hover {
    background: var(--color-primary-light);
    color: var(--color-button-text-light);
}

[data-theme="dark"] .btn-resend {
    color: var(--color-primary-dark);
    border-color: var(--color-primary-dark);
}

[data-theme="dark"] .btn-resend:hover {
    background: var(--color-primary-dark);
    color: var(--color-button-text-dark);
}

.email-info {
    text-align: center;
    margin-bottom: 1rem;
    padding: 0.5rem;
    background: rgba(46, 125, 50, 0.1);
    border-radius: 6px;
}

.verification-info {
    text-align: center;
    margin-bottom: 1.5rem;
    padding: 1rem;
    background: rgba(33, 150, 243, 0.1);
    border-radius: 8px;
    border-left: 4px solid #2196F3;
}

.verification-info p {
    margin: 0.5rem 0;
    color: var(--color-text-primary-light);
    line-height: 1.6;
}

[data-theme="dark"] .verification-info {
    background: rgba(33, 150, 243, 0.2);
}

[data-theme="dark"] .verification-info p {
    color: var(--color-text-primary-dark);
}

.email-info p {
    margin: 0;
    color: var(--color-text-secondary-light);
}

[data-theme="dark"] .email-info {
    background: rgba(46, 125, 50, 0.2);
}

[data-theme="dark"] .email-info p {
    color: var(--color-text-secondary-dark);
}
</style>
