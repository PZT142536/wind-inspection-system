<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="title">角色管理</h3>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增角色
      </el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="queryParams.keyword"
        placeholder="搜索角色名称/编码"
        clearable
        style="width: 200px;"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="code" label="角色编码" width="150" />
      <el-table-column prop="name" label="角色名称" width="150" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="primary" link @click="handlePermission(row)">权限</el-button>
          <el-button type="danger" link @click="handleDelete(row)" :disabled="row.code === 'ADMIN'">
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
        :page-sizes="[10, 20, 50]"
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
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" :disabled="!!form.id" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限分配对话框 -->
    <el-dialog v-model="permDialogVisible" title="分配权限" width="600px">
      <el-tree
        ref="permTreeRef"
        :data="permTree"
        :props="{ label: 'name', children: 'children' }"
        show-checkbox
        node-key="id"
        :default-checked-keys="selectedPermIds"
        default-expand-all
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handlePermSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, ElTree } from 'element-plus'
import {
  getRolePage,
  createRole,
  updateRole,
  deleteRole,
  changeRolePermissions,
  getRolePermissionIds,
  Role,
  RoleDTO,
} from '@/api/role'
import { getPermissionTree, Permission } from '@/api/permission'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<Role[]>([])
const total = ref(0)
const queryParams = reactive({
  keyword: '',
  pageNum: 1,
  pageSize: 10,
})

// 表单相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const form = reactive<RoleDTO & { id?: number }>({
  code: '',
  name: '',
  description: '',
})
const formRules = {
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
}

// 权限分配相关
const permDialogVisible = ref(false)
const permTree = ref<Permission[]>([])
const permTreeRef = ref<InstanceType<typeof ElTree>>()
const selectedPermIds = ref<number[]>([])
const currentRoleId = ref<number>(0)

onMounted(() => {
  handleSearch()
  loadPermTree()
})

async function handleSearch() {
  loading.value = true
  try {
    const res = await getRolePage(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function loadPermTree() {
  try {
    permTree.value = await getPermissionTree()
  } catch (error) {
    console.error('加载权限树失败', error)
  }
}

function handleAdd() {
  dialogTitle.value = '新增角色'
  form.id = undefined
  form.code = ''
  form.name = ''
  form.description = ''
  dialogVisible.value = true
}

function handleEdit(row: Role) {
  dialogTitle.value = '编辑角色'
  form.id = row.id
  form.code = row.code
  form.name = row.name
  form.description = row.description
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    if (form.id) {
      await updateRole(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createRole(form)
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

async function handleDelete(row: Role) {
  await ElMessageBox.confirm('确定要删除该角色吗？', '提示', { type: 'warning' })
  await deleteRole(row.id)
  ElMessage.success('删除成功')
  handleSearch()
}

async function handlePermission(row: Role) {
  currentRoleId.value = row.id
  try {
    const permIds = await getRolePermissionIds(row.id)
    selectedPermIds.value = permIds
    permDialogVisible.value = true
  } catch (error) {
    console.error('获取角色权限失败', error)
  }
}

async function handlePermSubmit() {
  const checkedKeys = permTreeRef.value?.getCheckedKeys(false) as number[]
  const halfCheckedKeys = permTreeRef.value?.getHalfCheckedKeys() as number[]
  const allKeys = [...checkedKeys, ...halfCheckedKeys]

  submitLoading.value = true
  try {
    await changeRolePermissions(currentRoleId.value, allKeys)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
  } finally {
    submitLoading.value = false
  }
}
</script>
