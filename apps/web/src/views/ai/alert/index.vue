<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="title">预警管理</h3>
      <el-button type="primary" @click="handleMarkAllRead" :disabled="unreadCount === 0">
        全部标记已读
      </el-button>
    </div>

    <!-- 筛选 -->
    <el-form :inline="true" class="filter-form">
      <el-form-item>
        <el-checkbox v-model="unreadOnly" @change="loadAlerts">仅显示未读</el-checkbox>
      </el-form-item>
    </el-form>

    <!-- 预警列表 -->
    <el-table :data="alerts" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.type === 'SAFETY_HELMET' ? 'warning' : 'danger'" size="small">
            {{ row.type === 'SAFETY_HELMET' ? '安全帽' : '吊带/夹具' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="message" label="预警内容" min-width="250" show-overflow-tooltip />
      <el-table-column prop="taskId" label="任务ID" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.readAt ? 'info' : 'danger'" size="small">
            {{ row.readAt ? '已读' : '未读' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发送时间" width="170">
        <template #default="{ row }">{{ formatTime(row.sentAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="!row.readAt"
            link type="primary" size="small"
            @click="handleMarkRead(row.id)"
          >标记已读</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.pageNum"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadAlerts"
      @current-change="loadAlerts"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAlertPage,
  markAlertRead,
  markAllAlertsRead,
  getUnreadAlertCount,
  type Alert
} from '@/api/alert'

const loading = ref(false)
const alerts = ref<Alert[]>([])
const unreadOnly = ref(false)
const unreadCount = ref(0)
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

onMounted(() => {
  loadAlerts()
  loadUnreadCount()
})

async function loadAlerts() {
  loading.value = true
  try {
    const res = await getAlertPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      unreadOnly: unreadOnly.value || undefined
    })
    alerts.value = res.records
    pagination.total = res.total
  } finally {
    loading.value = false
  }
}

async function loadUnreadCount() {
  try {
    unreadCount.value = await getUnreadAlertCount()
  } catch {}
}

async function handleMarkRead(id: number) {
  await markAlertRead(id)
  ElMessage.success('已标记已读')
  loadAlerts()
  loadUnreadCount()
}

async function handleMarkAllRead() {
  await ElMessageBox.confirm('确定将所有预警标记为已读？', '提示', { type: 'info' })
  await markAllAlertsRead()
  ElMessage.success('全部已标记已读')
  loadAlerts()
  loadUnreadCount()
}

function formatTime(time: string) {
  return time ? new Date(time).toLocaleString('zh-CN') : '-'
}
</script>

<style scoped>
.filter-form { margin-bottom: 16px; }
</style>
