<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="title">用户管理</h3>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="queryParams.keyword"
        placeholder="搜索工号/姓名"
        clearable
        style="width: 200px;"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px;">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
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
      <el-table-column prop="empNo" label="工号" width="120" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="dept" label="部门" width="150" />
      <el-table-column prop="source" label="来源" width="100">
        <template #default="{ row }">
          <el-tag :type="row.source === 'LDAP' ? 'success' : 'info'">
            {{ row.source }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录时间" width="180" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="primary" link @click="handleRole(row)">角色</el-button>
          <el-button type="primary" link @click="handleResetPwd(row)">重置密码</el-button>
          <el-button
            :type="row.status === 1 ? 'warning' : 'success'"
            link
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
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
      width="500px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="工号" prop="empNo">
          <el-input v-model="form.empNo" :disabled="!!form.id" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="部门" prop="dept">
          <el-input v-model="form.dept" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 角色分配对话框 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px">
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox v-for="role in allRoles" :key="role.id" :label="role.id">
          {{ role.name }}
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleRoleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import {
  getUserPage,
  createUser,
  updateUser,
  deleteUser,
  changeUserRoles,
  getUserRoleIds,
  lockUser,
  unlockUser,
  resetPassword,
  User,
  UserDTO,
} from '@/api/user'
import { getAllRoles, Role } from '@/api/role'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<User[]>([])
const total = ref(0)
const queryParams = reactive({
  keyword: '',
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10,
})

// 表单相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const form = reactive<UserDTO & { id?: number }>({
  empNo: '',
  name: '',
  dept: '',
  password: '',
})
const formRules = {
  empNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

// 角色分配相关
const roleDialogVisible = ref(false)
const allRoles = ref<Role[]>([])
const selectedRoleIds = ref<number[]>([])
const currentUserId = ref<number>(0)

onMounted(() => {
  handleSearch()
  loadAllRoles()
})

// 加载数据
async function handleSearch() {
  loading.value = true
  try {
    const res = await getUserPage(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

// 重置搜索
function handleReset() {
  queryParams.keyword = ''
  queryParams.status = undefined
  queryParams.pageNum = 1
  handleSearch()
}

// 加载所有角色
async function loadAllRoles() {
  try {
    allRoles.value = await getAllRoles()
  } catch (error) {
    console.error('加载角色失败', error)
  }
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增用户'
  form.id = undefined
  form.empNo = ''
  form.name = ''
  form.dept = ''
  form.password = ''
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: User) {
  dialogTitle.value = '编辑用户'
  form.id = row.id
  form.empNo = row.empNo
  form.name = row.name
  form.dept = row.dept
  form.password = ''
  dialogVisible.value = true
}

// 提交表单
async function handleSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    if (form.id) {
      await updateUser(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    handleSearch()
  } finally {
    submitLoading.value = false
  }
}

// 重置表单
function resetForm() {
  formRef.value?.resetFields()
}

// 删除
async function handleDelete(row: User) {
  await ElMessageBox.confirm('确定要删除该用户吗？', '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  handleSearch()
}

// 角色分配
async function handleRole(row: User) {
  currentUserId.value = row.id
  try {
    const roleIds = await getUserRoleIds(row.id)
    selectedRoleIds.value = roleIds
    roleDialogVisible.value = true
  } catch (error) {
    console.error('获取用户角色失败', error)
  }
}

// 提交角色分配
async function handleRoleSubmit() {
  submitLoading.value = true
  try {
    await changeUserRoles(currentUserId.value, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
  } finally {
    submitLoading.value = false
  }
}

// 重置密码
async function handleResetPwd(row: User) {
  await ElMessageBox.confirm(`确定要重置用户 ${row.name} 的密码吗？`, '提示', { type: 'warning' })
  await resetPassword(row.id, '123456')
  ElMessage.success('密码已重置为 123456')
}

// 切换状态
async function handleToggleStatus(row: User) {
  if (row.status === 1) {
    await lockUser(row.id)
    ElMessage.success('已禁用')
  } else {
    await unlockUser(row.id)
    ElMessage.success('已启用')
  }
  handleSearch()
}
</script>
