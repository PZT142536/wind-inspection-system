<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="title">日志管理</h3>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 登录日志 -->
      <el-tab-pane label="登录日志" name="login">
        <div class="search-bar">
          <el-input
            v-model="loginQuery.empNo"
            placeholder="搜索工号"
            clearable
            style="width: 150px;"
          />
          <el-select v-model="loginQuery.status" placeholder="状态" clearable style="width: 100px;">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
          <el-select v-model="loginQuery.source" placeholder="来源" clearable style="width: 100px;">
            <el-option label="WEB" value="WEB" />
            <el-option label="APP" value="APP" />
          </el-select>
          <el-button type="primary" @click="loadLoginLogs">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </div>

        <el-table :data="loginLogs" v-loading="loginLoading" border stripe>
          <el-table-column prop="empNo" label="工号" width="120" />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="ip" label="IP地址" width="150" />
          <el-table-column prop="source" label="来源" width="100">
            <template #default="{ row }">
              <el-tag :type="row.source === 'WEB' ? '' : 'success'">{{ row.source }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="failReason" label="失败原因" />
          <el-table-column prop="loginTime" label="登录时间" width="180" />
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="loginQuery.pageNum"
            v-model:page-size="loginQuery.pageSize"
            :total="loginTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadLoginLogs"
            @current-change="loadLoginLogs"
          />
        </div>
      </el-tab-pane>

      <!-- 操作日志 -->
      <el-tab-pane label="操作日志" name="operation">
        <div class="search-bar">
          <el-input
            v-model="operationQuery.empNo"
            placeholder="搜索工号"
            clearable
            style="width: 150px;"
          />
          <el-input
            v-model="operationQuery.module"
            placeholder="模块名称"
            clearable
            style="width: 150px;"
          />
          <el-select v-model="operationQuery.result" placeholder="结果" clearable style="width: 100px;">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAIL" />
          </el-select>
          <el-button type="primary" @click="loadOperationLogs">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </div>

        <el-table :data="operationLogs" v-loading="operationLoading" border stripe>
          <el-table-column prop="empNo" label="工号" width="120" />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="module" label="模块" width="120" />
          <el-table-column prop="action" label="操作" width="150" />
          <el-table-column prop="detail" label="详情" show-overflow-tooltip />
          <el-table-column prop="ip" label="IP地址" width="150" />
          <el-table-column prop="result" label="结果" width="100">
            <template #default="{ row }">
              <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'">
                {{ row.result === 'SUCCESS' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="操作时间" width="180" />
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="operationQuery.pageNum"
            v-model:page-size="operationQuery.pageSize"
            :total="operationTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadOperationLogs"
            @current-change="loadOperationLogs"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getLoginLogPage, getOperationLogPage, LoginLog, OperationLog } from '@/api/log'

const activeTab = ref('login')

// 登录日志
const loginLoading = ref(false)
const loginLogs = ref<LoginLog[]>([])
const loginTotal = ref(0)
const loginQuery = reactive({
  empNo: '',
  status: undefined as number | undefined,
  source: '',
  pageNum: 1,
  pageSize: 10,
})

// 操作日志
const operationLoading = ref(false)
const operationLogs = ref<OperationLog[]>([])
const operationTotal = ref(0)
const operationQuery = reactive({
  empNo: '',
  module: '',
  result: '',
  pageNum: 1,
  pageSize: 10,
})

onMounted(() => {
  loadLoginLogs()
})

function handleTabChange(tab: string) {
  if (tab === 'login') {
    loadLoginLogs()
  } else {
    loadOperationLogs()
  }
}

async function loadLoginLogs() {
  loginLoading.value = true
  try {
    const res = await getLoginLogPage(loginQuery)
    loginLogs.value = res.records
    loginTotal.value = res.total
  } finally {
    loginLoading.value = false
  }
}

async function loadOperationLogs() {
  operationLoading.value = true
  try {
    const res = await getOperationLogPage(operationQuery)
    operationLogs.value = res.records
    operationTotal.value = res.total
  } finally {
    operationLoading.value = false
  }
}
</script>
