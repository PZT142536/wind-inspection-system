<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>项目总数</span>
              <el-icon><Folder /></el-icon>
            </div>
          </template>
          <div class="stat-value">12</div>
          <div class="stat-label">进行中的项目</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>巡检任务</span>
              <el-icon><Tickets /></el-icon>
            </div>
          </template>
          <div class="stat-value">56</div>
          <div class="stat-label">待执行任务</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>AI发现</span>
              <el-icon><Warning /></el-icon>
            </div>
          </template>
          <div class="stat-value">23</div>
          <div class="stat-label">待处理缺陷</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>媒体文件</span>
              <el-icon><VideoCamera /></el-icon>
            </div>
          </template>
          <div class="stat-value">1,234</div>
          <div class="stat-label">已上传文件</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>巡检任务统计</span>
          </template>
          <div ref="taskChartRef" style="height: 400px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>最新预警</span>
          </template>
          <div class="alert-list">
            <div v-for="i in 5" :key="i" class="alert-item">
              <el-icon class="alert-icon" color="#E6A23C"><Warning /></el-icon>
              <div class="alert-content">
                <div class="alert-title">安全帽未佩戴检测</div>
                <div class="alert-time">2024-03-15 14:30:00</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const taskChartRef = ref<HTMLElement>()

onMounted(() => {
  initTaskChart()
})

function initTaskChart() {
  if (!taskChartRef.value) return

  const chart = echarts.init(taskChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
    },
    legend: {
      data: ['到货检查', '施工监督', '整体验收'],
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月'],
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '到货检查',
        type: 'bar',
        data: [12, 15, 18, 22, 25, 30],
      },
      {
        name: '施工监督',
        type: 'bar',
        data: [8, 10, 14, 16, 20, 24],
      },
      {
        name: '整体验收',
        type: 'bar',
        data: [5, 8, 10, 12, 15, 18],
      },
    ],
  }
  chart.setOption(option)

  window.addEventListener('resize', () => {
    chart.resize()
  })
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 0;
}

.stat-card {
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .stat-value {
    font-size: 32px;
    font-weight: 600;
    color: #333;
    text-align: center;
    padding: 10px 0;
  }

  .stat-label {
    font-size: 14px;
    color: #999;
    text-align: center;
  }
}

.alert-list {
  max-height: 400px;
  overflow-y: auto;
}

.alert-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .alert-icon {
    margin-top: 4px;
  }

  .alert-content {
    flex: 1;
  }

  .alert-title {
    font-size: 14px;
    color: #333;
  }

  .alert-time {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}
</style>
