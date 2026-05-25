<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="title">航线管理</h3>
      <div>
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回任务
        </el-button>
        <el-button type="primary" @click="handleGenerate" :disabled="!taskId">
          <el-icon><MagicStick /></el-icon>
          生成航线
        </el-button>
      </div>
    </div>

    <el-alert v-if="!taskId" type="warning" :closable="false" style="margin-bottom: 20px;">
      请从任务管理页面选择一个任务后再生成航线
    </el-alert>

    <el-table :data="routes" v-loading="loading" border stripe>
      <el-table-column prop="name" label="航线名称" min-width="200" />
      <el-table-column prop="surface" label="检查面" width="120" />
      <el-table-column prop="altitude" label="飞行高度(m)" width="120" />
      <el-table-column prop="speed" label="飞行速度(m/s)" width="120" />
      <el-table-column label="航点数量" width="100">
        <template #default="{ row }">
          {{ getWaypointCount(row) }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handlePreview(row)">预览</el-button>
          <el-button type="primary" link @click="handleExport(row)">导出</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 生成航线对话框 -->
    <el-dialog v-model="generateDialogVisible" title="生成航线" width="600px" @close="resetGenerateForm">
      <el-form ref="generateFormRef" :model="generateForm" :rules="generateFormRules" label-width="120px">
        <el-form-item label="生成类型" prop="genType">
          <el-select v-model="generateForm.genType" placeholder="选择生成类型" style="width: 100%;">
            <el-option label="叶片到货检查(3条航线)" value="blade_arrival" />
            <el-option label="轮毂到货检查(360度环绕)" value="hub_arrival" />
            <el-option label="机舱到货检查(360度环绕)" value="nacelle_arrival" />
            <el-option label="塔筒到货检查(360度环绕)" value="tower_arrival" />
            <el-option label="箱变到货检查(360度环绕)" value="box_transformer_arrival" />
            <el-option label="叶片吊装监督" value="blade_install" />
            <el-option label="风机整体验收" value="acceptance" />
          </el-select>
        </el-form-item>
        <el-form-item label="基准纬度" prop="baseLat">
          <el-input-number v-model="generateForm.baseLat" :precision="6" :step="0.000001" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="基准经度" prop="baseLng">
          <el-input-number v-model="generateForm.baseLng" :precision="6" :step="0.000001" style="width: 100%;" />
        </el-form-item>
        <el-form-item v-if="needHubHeight" label="轮毂高度(m)" prop="hubHeight">
          <el-input-number v-model="generateForm.hubHeight" :precision="1" :min="50" :max="200" style="width: 100%;" />
        </el-form-item>
        <el-form-item v-if="needBladeLength" label="叶片长度(m)" prop="bladeLength">
          <el-input-number v-model="generateForm.bladeLength" :precision="1" :min="30" :max="120" style="width: 100%;" />
        </el-form-item>
        <el-form-item v-if="needBladeNo" label="叶片编号" prop="bladeNo">
          <el-input v-model="generateForm.bladeNo" placeholder="请输入叶片编号" />
        </el-form-item>
        <el-form-item v-if="needTowerHeight" label="塔筒高度(m)" prop="towerHeight">
          <el-input-number v-model="generateForm.towerHeight" :precision="1" :min="50" :max="200" style="width: 100%;" />
        </el-form-item>
        <el-form-item v-if="needRadius" label="环绕半径(m)" prop="radius">
          <el-input-number v-model="generateForm.radius" :precision="1" :min="5" :max="100" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleGenerateSubmit">生成</el-button>
      </template>
    </el-dialog>

    <!-- 航线预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="航线预览" width="800px">
      <div class="route-preview">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="航线名称">{{ previewRoute?.name }}</el-descriptions-item>
          <el-descriptions-item label="检查面">{{ previewRoute?.surface }}</el-descriptions-item>
          <el-descriptions-item label="飞行高度">{{ previewRoute?.altitude }}m</el-descriptions-item>
          <el-descriptions-item label="飞行速度">{{ previewRoute?.speed }}m/s</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 16px 0 8px;">航点列表</h4>
        <el-table :data="previewWaypoints" border stripe size="small" max-height="400">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="latitude" label="纬度" width="120" />
          <el-table-column prop="longitude" label="经度" width="120" />
          <el-table-column prop="altitude" label="高度(m)" width="100" />
          <el-table-column prop="speed" label="速度(m/s)" width="100" />
          <el-table-column prop="heading" label="偏航角" width="80" />
          <el-table-column prop="gimbalPitch" label="俯仰角" width="80" />
          <el-table-column prop="stayTime" label="停留(s)" width="80" />
          <el-table-column prop="action" label="动作" width="120" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import {
  getRoutesByTask,
  generateBladeArrivalRoutes,
  generateOrbitalArrivalRoute,
  generateBladeInstallRoute,
  generateAcceptanceRoute,
  deleteRoute,
  FlightRoute,
} from '@/api/route'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const routes = ref<FlightRoute[]>([])
const taskId = ref<number>(Number(route.query.taskId) || 0)

// 生成表单相关
const generateDialogVisible = ref(false)
const generateFormRef = ref<FormInstance>()
const generateForm = reactive({
  genType: '',
  baseLat: 0,
  baseLng: 0,
  hubHeight: 80,
  bladeLength: 60,
  bladeNo: '',
  towerHeight: 80,
  radius: 30,
})
const generateFormRules = {
  genType: [{ required: true, message: '请选择生成类型', trigger: 'change' }],
  baseLat: [{ required: true, message: '请输入基准纬度', trigger: 'blur' }],
  baseLng: [{ required: true, message: '请输入基准经度', trigger: 'blur' }],
}

// 预览相关
const previewDialogVisible = ref(false)
const previewRoute = ref<FlightRoute | null>(null)
const previewWaypoints = ref<any[]>([])

// 计算属性
const needHubHeight = computed(() => ['blade_arrival', 'blade_install', 'acceptance'].includes(generateForm.genType))
const needBladeLength = computed(() => ['blade_arrival', 'blade_install'].includes(generateForm.genType))
const needBladeNo = computed(() => generateForm.genType === 'blade_install')
const needTowerHeight = computed(() => generateForm.genType === 'acceptance')
const needRadius = computed(() => ['hub_arrival', 'nacelle_arrival', 'tower_arrival', 'box_transformer_arrival'].includes(generateForm.genType))

onMounted(() => {
  if (taskId.value) {
    loadRoutes()
  }
})

async function loadRoutes() {
  loading.value = true
  try {
    routes.value = await getRoutesByTask(taskId.value)
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/inspection/task')
}

function handleGenerate() {
  generateForm.genType = ''
  generateForm.baseLat = 0
  generateForm.baseLng = 0
  generateDialogVisible.value = true
}

function resetGenerateForm() {
  generateFormRef.value?.resetFields()
}

async function handleGenerateSubmit() {
  const valid = await generateFormRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    const params = {
      taskId: taskId.value,
      baseLat: generateForm.baseLat,
      baseLng: generateForm.baseLng,
      hubHeight: generateForm.hubHeight,
      bladeLength: generateForm.bladeLength,
      bladeNo: generateForm.bladeNo,
      towerHeight: generateForm.towerHeight,
      radius: generateForm.radius,
      altitude: 30,
    }

    switch (generateForm.genType) {
      case 'blade_arrival':
        await generateBladeArrivalRoutes(params)
        break
      case 'hub_arrival':
        await generateOrbitalArrivalRoute({ ...params, component: 'HUB' })
        break
      case 'nacelle_arrival':
        await generateOrbitalArrivalRoute({ ...params, component: 'NACELLE' })
        break
      case 'tower_arrival':
        await generateOrbitalArrivalRoute({ ...params, component: 'TOWER' })
        break
      case 'box_transformer_arrival':
        await generateOrbitalArrivalRoute({ ...params, component: 'BOX_TRANSFORMER' })
        break
      case 'blade_install':
        await generateBladeInstallRoute(params)
        break
      case 'acceptance':
        await generateAcceptanceRoute(params)
        break
    }

    ElMessage.success('航线生成成功')
    generateDialogVisible.value = false
    await loadRoutes()
  } finally {
    submitLoading.value = false
  }
}

function getWaypointCount(route: FlightRoute) {
  try {
    const waypoints = JSON.parse(route.waypointsJson)
    return waypoints.length
  } catch {
    return '-'
  }
}

function handlePreview(route: FlightRoute) {
  previewRoute.value = route
  try {
    previewWaypoints.value = JSON.parse(route.waypointsJson)
  } catch {
    previewWaypoints.value = []
  }
  previewDialogVisible.value = true
}

function handleExport(route: FlightRoute) {
  // 导出DJI格式JSON
  const data = route.djiMissionJson
  const blob = new Blob([data], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${route.name}.json`
  a.click()
  URL.revokeObjectURL(url)
}

async function handleDelete(row: FlightRoute) {
  await ElMessageBox.confirm('确定要删除该航线吗？', '提示', { type: 'warning' })
  await deleteRoute(row.id)
  ElMessage.success('删除成功')
  await loadRoutes()
}
</script>

<style lang="scss" scoped>
.route-preview {
  max-height: 600px;
  overflow-y: auto;
}
</style>
