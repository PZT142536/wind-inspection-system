<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="title">任务管理</h3>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增任务
      </el-button>
    </div>

    <div class="search-bar">
      <el-select v-model="queryParams.projectId" placeholder="选择项目" clearable style="width: 200px;">
        <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
      </el-select>
      <el-select v-model="queryParams.type" placeholder="任务类型" clearable style="width: 150px;">
        <el-option label="到货检查" value="ARRIVAL" />
        <el-option label="施工监督" value="INSTALL" />
        <el-option label="整体验收" value="ACCEPTANCE" />
      </el-select>
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px;">
        <el-option label="待执行" value="PENDING" />
        <el-option label="执行中" value="IN_PROGRESS" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已失败" value="FAILED" />
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button @click="handleReset">
        <el-icon><Refresh /></el-icon>
        重置
      </el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="任务ID" width="100" />
      <el-table-column label="项目" width="150">
        <template #default="{ row }">
          {{ getProjectName(row.projectId) }}
        </template>
      </el-table-column>
      <el-table-column prop="type" label="任务类型" width="120">
        <template #default="{ row }">
          <el-tag :type="typeTagType(row.type)">{{ typeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="component" label="部套" width="120">
        <template #default="{ row }">
          {{ componentLabel(row.component) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="进度" width="150">
        <template #default="{ row }">
          <el-progress :percentage="row.progress" :status="row.progress === 100 ? 'success' : ''" />
        </template>
      </el-table-column>
      <el-table-column prop="plannedAt" label="计划时间" width="180" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleRoute(row)">航线</el-button>
          <el-button
            v-if="row.status === 'PENDING'"
            type="success" link @click="handleStart(row)"
          >开始</el-button>
          <el-button
            v-if="row.status === 'IN_PROGRESS'"
            type="warning" link @click="handleComplete(row)"
          >完成</el-button>
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row)" :disabled="row.status === 'IN_PROGRESS'">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="选择项目" style="width: 100%;">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="机位" prop="turbineId">
          <el-select v-model="form.turbineId" placeholder="选择机位" clearable style="width: 100%;">
            <el-option v-for="t in turbines" :key="t.id" :label="t.code" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务类型" prop="type">
          <el-select v-model="form.type" placeholder="选择任务类型" style="width: 100%;">
            <el-option label="到货检查" value="ARRIVAL" />
            <el-option label="施工监督" value="INSTALL" />
            <el-option label="整体验收" value="ACCEPTANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="部套" prop="component" v-if="form.type === 'ARRIVAL'">
          <el-select v-model="form.component" placeholder="选择部套" style="width: 100%;">
            <el-option label="叶片" value="BLADE" />
            <el-option label="轮毂" value="HUB" />
            <el-option label="机舱" value="NACELLE" />
            <el-option label="塔筒" value="TOWER" />
            <el-option label="箱变" value="BOX_TRANSFORMER" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划时间" prop="plannedAt">
          <el-date-picker v-model="form.plannedAt" type="datetime" placeholder="选择计划时间" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { getTaskPage, createTask, updateTask, deleteTask, startTask, completeTask, InspectionTask, TaskDTO } from '@/api/task'
import { getAllProjects, Project } from '@/api/project'
import { getTurbinesByProject, Turbine } from '@/api/turbine'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<InspectionTask[]>([])
const total = ref(0)
const projects = ref<Project[]>([])
const turbines = ref<Turbine[]>([])

const queryParams = reactive({
  projectId: undefined as number | undefined,
  type: '',
  status: '',
  pageNum: 1,
  pageSize: 10,
})

// 表单相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const form = reactive<TaskDTO & { id?: number }>({
  projectId: 0,
  turbineId: undefined,
  type: '',
  component: '',
  plannedAt: '',
  remark: '',
})
const formRules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  type: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
}

// 监听项目变化，加载机位列表
watch(() => form.projectId, async (val) => {
  if (val) {
    turbines.value = await getTurbinesByProject(val)
  } else {
    turbines.value = []
  }
})

onMounted(() => {
  handleSearch()
  loadProjects()
})

async function loadProjects() {
  projects.value = await getAllProjects()
}

function getProjectName(projectId: number) {
  const p = projects.value.find(item => item.id === projectId)
  return p?.name || projectId
}

function typeTagType(type: string) {
  const map: Record<string, string> = { ARRIVAL: '', INSTALL: 'warning', ACCEPTANCE: 'success' }
  return map[type] || 'info'
}

function typeLabel(type: string) {
  const map: Record<string, string> = { ARRIVAL: '到货检查', INSTALL: '施工监督', ACCEPTANCE: '整体验收' }
  return map[type] || type
}

function componentLabel(component: string) {
  const map: Record<string, string> = {
    BLADE: '叶片', HUB: '轮毂', NACELLE: '机舱', TOWER: '塔筒', BOX_TRANSFORMER: '箱变', ALL: '全部'
  }
  return map[component] || component || '-'
}

function statusTagType(status: string) {
  const map: Record<string, string> = {
    PENDING: 'info', IN_PROGRESS: 'primary', COMPLETED: 'success', FAILED: 'danger', CANCELLED: 'warning'
  }
  return map[status] || 'info'
}

function statusLabel(status: string) {
  const map: Record<string, string> = {
    PENDING: '待执行', IN_PROGRESS: '执行中', COMPLETED: '已完成', FAILED: '已失败', CANCELLED: '已取消'
  }
  return map[status] || status
}

async function handleSearch() {
  loading.value = true
  try {
    const res = await getTaskPage(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryParams.projectId = undefined
  queryParams.type = ''
  queryParams.status = ''
  queryParams.pageNum = 1
  handleSearch()
}

function handleAdd() {
  dialogTitle.value = '新增任务'
  form.id = undefined
  form.projectId = 0
  form.turbineId = undefined
  form.type = ''
  form.component = ''
  form.plannedAt = ''
  form.remark = ''
  dialogVisible.value = true
}

function handleEdit(row: InspectionTask) {
  dialogTitle.value = '编辑任务'
  form.id = row.id
  form.projectId = row.projectId
  form.turbineId = row.turbineId
  form.type = row.type
  form.component = row.component
  form.plannedAt = row.plannedAt
  form.remark = row.remark
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    if (form.id) {
      await updateTask(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createTask(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    handleSearch()
  } finally {
    submitLoading.value = false
  }
}

function resetForm() {
  formRef.value?.resetFields()
}

async function handleDelete(row: InspectionTask) {
  await ElMessageBox.confirm('确定要删除该任务吗？', '提示', { type: 'warning' })
  await deleteTask(row.id)
  ElMessage.success('删除成功')
  handleSearch()
}

async function handleStart(row: InspectionTask) {
  await ElMessageBox.confirm('确定要开始该任务吗？', '提示', { type: 'info' })
  await startTask(row.id)
  ElMessage.success('任务已开始')
  handleSearch()
}

async function handleComplete(row: InspectionTask) {
  await ElMessageBox.confirm('确定要完成该任务吗？', '提示', { type: 'info' })
  await completeTask(row.id)
  ElMessage.success('任务已完成')
  handleSearch()
}

function handleRoute(row: InspectionTask) {
  router.push(`/inspection/route?taskId=${row.id}`)
}
</script>
