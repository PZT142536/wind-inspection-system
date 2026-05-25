<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="title">AI缺陷发现</h3>
    </div>

    <!-- 筛选条件 -->
    <el-form :inline="true" class="filter-form">
      <el-form-item label="任务ID">
        <el-input v-model="filter.taskId" placeholder="输入任务ID" clearable />
      </el-form-item>
      <el-form-item label="检测类型">
        <el-select v-model="filter.type" placeholder="全部" clearable>
          <el-option label="安全帽" value="SAFETY_HELMET" />
          <el-option label="吊带/夹具" value="SLING_POSITION" />
        </el-select>
      </el-form-item>
      <el-form-item label="严重程度">
        <el-select v-model="filter.severity" placeholder="全部" clearable>
          <el-option label="高" value="HIGH" />
          <el-option label="中" value="MEDIUM" />
          <el-option label="低" value="LOW" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="filter.status" placeholder="全部" clearable>
          <el-option label="待审核" value="PENDING" />
          <el-option label="已确认" value="CONFIRMED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadFindings">查询</el-button>
        <el-button @click="resetFilter">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row" v-if="stats">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总发现数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-high">
          <div class="stat-value">{{ stats.highSeverity }}</div>
          <div class="stat-label">高严重度</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-medium">
          <div class="stat-value">{{ stats.mediumSeverity }}</div>
          <div class="stat-label">中严重度</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-low">
          <div class="stat-value">{{ stats.pending }}</div>
          <div class="stat-label">待审核</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 发现列表 -->
    <el-table :data="findings" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="type" label="检测类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.type === 'SAFETY_HELMET' ? 'warning' : 'danger'" size="small">
            {{ row.type === 'SAFETY_HELMET' ? '安全帽' : '吊带/夹具' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="severity" label="严重程度" width="100">
        <template #default="{ row }">
          <el-tag :type="severityType(row.severity)" size="small">
            {{ severityLabel(row.severity) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="confidence" label="置信度" width="90">
        <template #default="{ row }">
          {{ (row.confidence * 100).toFixed(1) }}%
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="taskId" label="任务ID" width="90" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发现时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'PENDING'"
            link type="success" size="small"
            @click="handleReview(row.id, 'CONFIRMED')"
          >确认</el-button>
          <el-button
            v-if="row.status === 'PENDING'"
            link type="danger" size="small"
            @click="handleReview(row.id, 'REJECTED')"
          >驳回</el-button>
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
      @size-change="loadFindings"
      @current-change="loadFindings"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAiFindingPage,
  reviewAiFinding,
  getAiTaskStats,
  type AiFinding,
  type AiTaskStats
} from '@/api/ai'

const loading = ref(false)
const findings = ref<AiFinding[]>([])
const stats = ref<AiTaskStats | null>(null)
const filter = reactive({ taskId: '', type: '', severity: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

onMounted(() => loadFindings())

async function loadFindings() {
  loading.value = true
  try {
    const res = await getAiFindingPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      taskId: filter.taskId ? Number(filter.taskId) : undefined,
      type: filter.type || undefined,
      severity: filter.severity || undefined,
      status: filter.status || undefined
    })
    findings.value = res.records
    pagination.total = res.total

    // 如果有taskId筛选，加载统计
    if (filter.taskId) {
      stats.value = await getAiTaskStats(Number(filter.taskId))
    } else {
      stats.value = null
    }
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  filter.taskId = ''
  filter.type = ''
  filter.severity = ''
  filter.status = ''
  stats.value = null
  loadFindings()
}

async function handleReview(id: number, status: string) {
  const label = status === 'CONFIRMED' ? '确认' : '驳回'
  await ElMessageBox.confirm(`确定${label}该发现？`, '提示', { type: 'warning' })
  await reviewAiFinding(id, status)
  ElMessage.success(`${label}成功`)
  loadFindings()
}

function severityType(severity: string) {
  return severity === 'HIGH' ? 'danger' : severity === 'MEDIUM' ? 'warning' : 'info'
}

function severityLabel(severity: string) {
  return severity === 'HIGH' ? '高' : severity === 'MEDIUM' ? '中' : '低'
}

function statusType(status: string) {
  return status === 'CONFIRMED' ? 'success' : status === 'REJECTED' ? 'danger' : 'info'
}

function statusLabel(status: string) {
  return status === 'CONFIRMED' ? '已确认' : status === 'REJECTED' ? '已驳回' : '待审核'
}

function formatTime(time: string) {
  return time ? new Date(time).toLocaleString('zh-CN') : '-'
}
</script>

<style scoped>
.filter-form { margin-bottom: 16px; }
.stats-row { margin-bottom: 16px; }
.stat-card { text-align: center; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.stat-high .stat-value { color: #f56c6c; }
.stat-medium .stat-value { color: #e6a23c; }
.stat-low .stat-value { color: #909399; }
</style>
