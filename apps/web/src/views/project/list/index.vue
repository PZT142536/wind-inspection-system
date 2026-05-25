<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="title">项目列表</h3>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增项目
      </el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="queryParams.keyword"
        placeholder="搜索项目名称/编码"
        clearable
        style="width: 200px;"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px;">
        <el-option label="进行中" value="ACTIVE" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已归档" value="ARCHIVED" />
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
      <el-table-column prop="code" label="项目编码" width="120" />
      <el-table-column prop="name" label="项目名称" min-width="150" />
      <el-table-column prop="owner" label="业主" width="120" />
      <el-table-column prop="location" label="位置" width="150" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
          <el-button type="primary" link @click="handleTurbine(row)">机位</el-button>
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入项目编码" />
        </el-form-item>
        <el-form-item label="业主" prop="owner">
          <el-input v-model="form.owner" placeholder="请输入业主名称" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入项目位置" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="纬度" prop="lat">
              <el-input-number v-model="form.lat" :precision="6" :step="0.000001" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度" prop="lng">
              <el-input-number v-model="form.lng" :precision="6" :step="0.000001" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 机位管理对话框 -->
    <el-dialog v-model="turbineDialogVisible" title="机位管理" width="900px">
      <div class="table-toolbar">
        <span>项目: {{ currentProject?.name }}</span>
        <el-button type="primary" size="small" @click="handleAddTurbine">
          <el-icon><Plus /></el-icon>
          新增机位
        </el-button>
      </div>

      <el-table :data="turbines" v-loading="turbineLoading" border stripe size="small">
        <el-table-column prop="code" label="机位编号" width="100" />
        <el-table-column prop="model" label="机型" width="120" />
        <el-table-column prop="lat" label="纬度" width="120" />
        <el-table-column prop="lng" label="经度" width="120" />
        <el-table-column prop="hubHeight" label="轮毂高度(m)" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'INSTALLED' ? 'success' : 'info'" size="small">
              {{ row.status === 'INSTALLED' ? '已安装' : '待安装' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditTurbine(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteTurbine(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 机位编辑对话框 -->
    <el-dialog
      v-model="turbineFormVisible"
      :title="turbineFormTitle"
      width="500px"
      @close="resetTurbineForm"
    >
      <el-form ref="turbineFormRef" :model="turbineForm" :rules="turbineFormRules" label-width="100px">
        <el-form-item label="机位编号" prop="code">
          <el-input v-model="turbineForm.code" placeholder="请输入机位编号" />
        </el-form-item>
        <el-form-item label="机型" prop="model">
          <el-input v-model="turbineForm.model" placeholder="请输入机型" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="纬度" prop="lat">
              <el-input-number v-model="turbineForm.lat" :precision="6" :step="0.000001" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度" prop="lng">
              <el-input-number v-model="turbineForm.lng" :precision="6" :step="0.000001" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="海拔高度">
              <el-input-number v-model="turbineForm.altitude" :precision="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="轮毂高度">
              <el-input-number v-model="turbineForm.hubHeight" :precision="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="turbineFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleTurbineSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { getProjectPage, createProject, updateProject, deleteProject, Project, ProjectDTO } from '@/api/project'
import { getTurbinesByProject, createTurbine, updateTurbine, deleteTurbine, Turbine, TurbineDTO } from '@/api/turbine'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<Project[]>([])
const total = ref(0)
const queryParams = reactive({
  keyword: '',
  status: '',
  pageNum: 1,
  pageSize: 10,
})

// 项目表单相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const form = reactive<ProjectDTO & { id?: number }>({
  name: '',
  code: '',
  owner: '',
  location: '',
  lat: undefined,
  lng: undefined,
  description: '',
})
const formRules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
}

// 机位相关
const turbineDialogVisible = ref(false)
const turbineLoading = ref(false)
const currentProject = ref<Project | null>(null)
const turbines = ref<Turbine[]>([])

// 机位表单相关
const turbineFormVisible = ref(false)
const turbineFormTitle = ref('')
const turbineFormRef = ref<FormInstance>()
const turbineForm = reactive<TurbineDTO & { id?: number }>({
  projectId: 0,
  code: '',
  model: '',
  lat: 0,
  lng: 0,
  altitude: undefined,
  hubHeight: undefined,
})
const turbineFormRules = {
  code: [{ required: true, message: '请输入机位编号', trigger: 'blur' }],
  lat: [{ required: true, message: '请输入纬度', trigger: 'blur' }],
  lng: [{ required: true, message: '请输入经度', trigger: 'blur' }],
}

onMounted(() => {
  handleSearch()
})

function statusTagType(status: string) {
  const map: Record<string, string> = {
    ACTIVE: 'primary',
    COMPLETED: 'success',
    ARCHIVED: 'info',
  }
  return map[status] || 'info'
}

function statusLabel(status: string) {
  const map: Record<string, string> = {
    ACTIVE: '进行中',
    COMPLETED: '已完成',
    ARCHIVED: '已归档',
  }
  return map[status] || status
}

async function handleSearch() {
  loading.value = true
  try {
    const res = await getProjectPage(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryParams.keyword = ''
  queryParams.status = ''
  queryParams.pageNum = 1
  handleSearch()
}

function handleAdd() {
  dialogTitle.value = '新增项目'
  form.id = undefined
  form.name = ''
  form.code = ''
  form.owner = ''
  form.location = ''
  form.lat = undefined
  form.lng = undefined
  form.description = ''
  dialogVisible.value = true
}

function handleEdit(row: Project) {
  dialogTitle.value = '编辑项目'
  form.id = row.id
  form.name = row.name
  form.code = row.code
  form.owner = row.owner
  form.location = row.location
  form.lat = row.lat
  form.lng = row.lng
  form.description = row.description
  dialogVisible.value = true
}

function handleDetail(row: Project) {
  router.push(`/project/detail/${row.id}`)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    if (form.id) {
      await updateProject(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createProject(form)
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

async function handleDelete(row: Project) {
  await ElMessageBox.confirm('确定要删除该项目吗？删除后将无法恢复。', '提示', { type: 'warning' })
  await deleteProject(row.id)
  ElMessage.success('删除成功')
  handleSearch()
}

// 机位管理
async function handleTurbine(row: Project) {
  currentProject.value = row
  turbineDialogVisible.value = true
  await loadTurbines(row.id)
}

async function loadTurbines(projectId: number) {
  turbineLoading.value = true
  try {
    turbines.value = await getTurbinesByProject(projectId)
  } finally {
    turbineLoading.value = false
  }
}

function handleAddTurbine() {
  turbineFormTitle.value = '新增机位'
  turbineForm.id = undefined
  turbineForm.projectId = currentProject.value!.id
  turbineForm.code = ''
  turbineForm.model = ''
  turbineForm.lat = currentProject.value!.lat || 0
  turbineForm.lng = currentProject.value!.lng || 0
  turbineForm.altitude = undefined
  turbineForm.hubHeight = undefined
  turbineFormVisible.value = true
}

function handleEditTurbine(row: Turbine) {
  turbineFormTitle.value = '编辑机位'
  turbineForm.id = row.id
  turbineForm.projectId = row.projectId
  turbineForm.code = row.code
  turbineForm.model = row.model
  turbineForm.lat = row.lat
  turbineForm.lng = row.lng
  turbineForm.altitude = row.altitude
  turbineForm.hubHeight = row.hubHeight
  turbineFormVisible.value = true
}

async function handleTurbineSubmit() {
  const valid = await turbineFormRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    if (turbineForm.id) {
      await updateTurbine(turbineForm.id, turbineForm)
      ElMessage.success('更新成功')
    } else {
      await createTurbine(turbineForm)
      ElMessage.success('创建成功')
    }
    turbineFormVisible.value = false
    await loadTurbines(currentProject.value!.id)
  } finally {
    submitLoading.value = false
  }
}

function resetTurbineForm() {
  turbineFormRef.value?.resetFields()
}

async function handleDeleteTurbine(row: Turbine) {
  await ElMessageBox.confirm('确定要删除该机位吗？', '提示', { type: 'warning' })
  await deleteTurbine(row.id)
  ElMessage.success('删除成功')
  await loadTurbines(currentProject.value!.id)
}
</script>
