<template>
  <div class="report-problem">
    <div class="header">
      <h1>{{ $t('report.title') }}</h1>
      <p>{{ $t('report.subtitle') }}</p>
    </div>

    <form @submit.prevent="submitReport" class="report-form">
      <div class="form-section">
        <h3>{{ $t('report.bikeInfo') }}</h3>

        <div class="form-group">
          <label class="label">{{ $t('report.bikeId') }}</label>
          <input
            v-model="formData.bikeId"
            type="text"
            placeholder="Ej: BIC-1234"
            class="form-input"
            required
          >
        </div>

        <div class="form-group">
          <label class="label">{{ $t('report.stationWhere') }}</label>
          <select v-model="formData.stationId" class="form-select" required>
            <option value="">{{ $t('report.selectStation') }}</option>
            <option
              v-for="station in stations"
              :key="station.idStation"
              :value="station.idStation"
            >
              {{ station.nameStation }}
            </option>
          </select>
        </div>
      </div>

      <div class="form-section">
        <h3>{{ $t('report.problemDetails') }}</h3>

        <div class="form-group">
          <label class="label">{{ $t('report.problemType') }}</label>
          <div class="problem-types">
            <label
              v-for="problem in problemTypes"
              :key="problem.id"
              class="problem-card"
              :class="{ 'selected': formData.problemType === problem.id }"
            >
              <input
                type="radio"
                v-model="formData.problemType"
                :value="problem.id"
                class="radio-input"
              >
              <div class="problem-content">
                <span class="problem-icon">{{ problem.icon }}</span>
                <span class="problem-name">{{ $t('report.problems.' + problem.id) }}</span>
              </div>
            </label>
          </div>
        </div>

        <div class="form-group">
          <label class="label">{{ $t('report.severity') }}</label>
          <div class="severity-levels">
            <label
              v-for="level in severityLevels"
              :key="level.id"
              class="severity-card"
              :class="{
                'selected': formData.severity === level.id,
                [level.id]: true
              }"
            >
              <input
                type="radio"
                v-model="formData.severity"
                :value="level.id"
                class="radio-input"
              >
              <div class="severity-content">
                <span class="severity-name">{{ $t('report.severityLevels.' + level.id + '.name') }}</span>
                <span class="severity-desc">{{ $t('report.severityLevels.' + level.id + '.desc') }}</span>
              </div>
            </label>
          </div>
        </div>

        <div class="form-group">
          <label class="label">{{ $t('report.description') }}</label>
          <textarea
            v-model="formData.description"
            :placeholder="$t('report.descriptionPlaceholder')"
            class="form-textarea"
            rows="4"
            required
          ></textarea>
        </div>

        <div class="form-group">
          <label class="label">{{ $t('report.allowsUse') }}</label>
          <div class="radio-group">
            <label class="radio-label">
              <input
                type="radio"
                v-model="formData.allowsUse"
                :value="false"
                class="radio-input"
              >
              <span>{{ $t('report.noUse') }}</span>
            </label>
            <label class="radio-label">
              <input
                type="radio"
                v-model="formData.allowsUse"
                :value="true"
                class="radio-input"
              >
              <span>{{ $t('report.yesUse') }}</span>
            </label>
          </div>
        </div>
      </div>

      <div class="form-actions">
        <button
          type="button"
          class="btn-secondary"
          @click="resetForm"
        >
          {{ $t('common.cancel') }}
        </button>
        <button
          type="submit"
          class="butn-primary"
          :disabled="loading"
        >
          {{ loading ? $t('report.sending') : $t('report.sendReport') }}
        </button>
      </div>
    </form>

    <!-- Confirmation -->
    <div v-if="showSuccess" class="modal-overlay">
      <div class="modal">
        <div class="modal-content">
          <div class="success-icon">✅</div>
          <h3>{{ $t('report.successTitle') }}</h3>
          <p>{{ $t('report.successMsg') }}</p>
          <p class="report-id">{{ $t('report.reportId') }} <strong>{{ reportId }}</strong></p>
          <button class="butn-primary" @click="closeModal">
            {{ $t('common.accept') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n';

interface ProblemForm {
  bikeId: string
  stationId: string
  problemType: string
  severity: string
  description: string
  allowsUse: boolean | null
}

const loading = ref(false)
const showSuccess = ref(false)
const reportId = ref('')

// Datos de ejemplo - usando modelo Station correcto
const stations = ref([
  { idStation: 1, nameStation: 'Estación Centro' },
  { idStation: 2, nameStation: 'Parque Sikuani' },
  { idStation: 3, nameStation: 'Zona Universitaria' },
  { idStation: 4, nameStation: 'Estación Sur' }
])

const problemTypes = ref([
  { id: 'mechanical', icon: '🔧' },
  { id: 'electrical', icon: '🔌' },
  { id: 'brakes', icon: '🛑' },
  { id: 'tire', icon: '🚲' },
  { id: 'chain', icon: '⛓️' },
  { id: 'other', icon: '❓' }
])

const severityLevels = ref([
  { id: 'low' },
  { id: 'medium' },
  { id: 'high' }
])

const formData = reactive<ProblemForm>({
  bikeId: '',
  stationId: '',
  problemType: '',
  severity: '',
  description: '',
  allowsUse: null
})

const { t: $t } = useI18n();
const submitReport = async () => {
  if (loading.value) return

  loading.value = true

  try {
    // Simular a API
    await new Promise(resolve => setTimeout(resolve, 1500))

    // Generar ID de reporte
    reportId.value = 'RP-' + Date.now().toString().slice(-6)
    showSuccess.value = true

    // Would be sent to the API
    console.log('Reporte enviado:', formData)

  } catch (error) {
    console.error('Error enviando reporte:', error)
    alert($t('report.sendError'))
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  if (confirm($t('report.cancelConfirm') as string)) {
    formData.bikeId = ''
    formData.stationId = ''
    formData.problemType = ''
    formData.severity = ''
    formData.description = ''
    formData.allowsUse = null
  }
}

const closeModal = () => {
  showSuccess.value = false
  resetForm()
}
</script>

<style lang="scss" scoped>
.report-problem {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.header {
  margin-bottom: 30px;

  h1 {
    margin: 0 0 8px 0;
    color: #333;
  }

  p {
    margin: 0;
    color: #666;
  }
}

.report-form {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.form-section {
  margin-bottom: 30px;

  h3 {
    margin: 0 0 20px 0;
    color: #333;
    border-bottom: 2px solid #f0f0f0;
    padding-bottom: 10px;
  }
}

.form-group {
  margin-bottom: 25px;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;

  &:focus {
    outline: none;
    border-color: #007bff;
  }
}

.form-textarea {
  resize: vertical;
  font-family: inherit;
}

.problem-types,
.severity-levels {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
}

.problem-card,
.severity-card {
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s ease;

  &.selected {
    border-color: #007bff;
    background: #f8fbff;
  }

  &:hover {
    border-color: #007bff;
  }
}

.problem-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;

  .problem-icon {
    font-size: 24px;
    margin-bottom: 8px;
  }

  .problem-name {
    font-weight: 500;
    color: #333;
  }
}

.severity-content {
  .severity-name {
    display: block;
    font-weight: 600;
    margin-bottom: 4px;
  }

  .severity-desc {
    display: block;
    font-size: 12px;
    color: #666;
  }
}

// Estilos para niveles de gravedad
.severity-card.low.selected {
  border-color: #28a745;
  background: #f8fff9;
}

.severity-card.medium.selected {
  border-color: #ffc107;
  background: #fffdf4;
}

.severity-card.high.selected {
  border-color: #dc3545;
  background: #fff5f5;
}

.radio-input {
  display: none;
}

.radio-group {
  display: flex;
  gap: 20px;
}

.radio-label {
  display: flex;
  align-items: center;
  cursor: pointer;

  span {
    margin-left: 8px;
  }
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

// Success modal
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 12px;
  padding: 30px;
  max-width: 400px;
  width: 90%;
  text-align: center;
}

.success-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.modal-content h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.modal-content p {
  margin: 0 0 15px 0;
  color: #666;
  line-height: 1.5;
}

.report-id {
  background: #f8f9fa;
  padding: 10px;
  border-radius: 6px;
  font-family: monospace;
}

.butn-primary:disabled {
  background: #6c757d;
  cursor: not-allowed;
}
</style>
