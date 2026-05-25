<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="title">媒体文件管理</h3>
      <el-button type="primary" @click="showUploadDialog = true">
        <el-icon><Upload /></el-icon>
        上传文件
      </el-button>
    </div>

    <!-- 筛选条件 -->
    <el-form :inline="true" class="filter-form">
      <el-form-item label="任务ID">
        <el-input v-model="filter.taskId" placeholder="输入任务ID" clearable />
      </el-form-item>
      <el-form-item label="文件类型">
        <el-select v-model="filter.type" placeholder="全部" clearable>
          <el-option label="视频" value="VIDEO" />
          <el-option label="图片" value="PHOTO" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadFiles">查询</el-button>
        <el-button @click="resetFilter">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 文件列表 -->
    <el-table :data="files" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="originalName" label="文件名" min-width="200" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.type === 'VIDEO' ? 'primary' : 'success'" size="small">
            {{ row.type === 'VIDEO' ? '视频' : '图片' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="大小" width="100">
        <template #default="{ row }">
          {{ formatSize(row.sizeBytes) }}
        </template>
      </el-table-column>
      <el-table-column prop="surface" label="巡检面" width="100" />
      <el-table-column prop="keyPoint" label="关键点" width="100" />
      <el-table-column prop="taskId" label="任务ID" width="100" />
      <el-table-column label="上传时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.uploadedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handlePreview(row)">预览</el-button>
          <el-button link type="primary" size="small" @click="handleDownload(row)">下载</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
      @size-change="loadFiles"
      @current-change="loadFiles"
    />

    <!-- 上传对话框 -->
    <el-dialog v-model="showUploadDialog" title="上传文件" width="600px">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="任务ID" required>
          <el-input-number v-model="uploadForm.taskId" :min="1" />
        </el-form-item>
        <el-form-item label="文件类型" required>
          <el-radio-group v-model="uploadForm.fileType">
            <el-radio value="VIDEO">视频</el-radio>
            <el-radio value="PHOTO">图片</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="巡检面">
          <el-input v-model="uploadForm.surface" placeholder="如：pressure/suction/leading" />
        </el-form-item>
        <el-form-item label="关键点">
          <el-input v-model="uploadForm.keyPoint" placeholder="如：root/mid/tip" />
        </el-form-item>
        <el-form-item label="选择文件" required>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            drag
          >
            <el-icon class="el-icon--upload"><Upload /></el-icon>
            <div class="el-upload__text">拖拽文件到此处或 <em>点击选择</em></div>
            <template #tip>
              <div class="el-upload__tip">支持大文件分片上传，自动断点续传</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <!-- 上传进度 -->
      <div v-if="uploading" class="upload-progress">
        <el-progress
          :percentage="uploadProgress"
          :status="uploadProgress === 100 ? 'success' : undefined"
          :stroke-width="20"
          :text-inside="true"
        />
        <p class="progress-text">{{ uploadStatusText }}</p>
      </div>

      <template #footer>
        <el-button @click="showUploadDialog = false" :disabled="uploading">取消</el-button>
        <el-button type="primary" @click="startUpload" :loading="uploading" :disabled="!selectedFile">
          {{ uploading ? '上传中...' : '开始上传' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog v-model="showPreviewDialog" title="文件预览" width="800px">
      <div v-if="previewUrl" class="preview-container">
        <video
          v-if="previewType === 'VIDEO'"
          :src="previewUrl"
          controls
          style="width: 100%"
        />
        <img
          v-else
          :src="previewUrl"
          style="max-width: 100%"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import SparkMD5 from 'spark-md5'
import {
  getMediaFilePage,
  deleteMediaFile,
  getDownloadUrl,
  getPreviewUrl,
  initUpload,
  uploadChunk,
  mergeChunks,
  type MediaFile
} from '@/api/media'

const loading = ref(false)
const files = ref<MediaFile[]>([])
const filter = reactive({ taskId: '', type: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const showUploadDialog = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadStatusText = ref('')
const selectedFile = ref<File | null>(null)
const uploadForm = reactive({
  taskId: 1,
  fileType: 'VIDEO',
  surface: '',
  keyPoint: ''
})

const showPreviewDialog = ref(false)
const previewUrl = ref('')
const previewType = ref('')

const CHUNK_SIZE = 5 * 1024 * 1024 // 5MB

onMounted(() => loadFiles())

async function loadFiles() {
  loading.value = true
  try {
    const res = await getMediaFilePage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      taskId: filter.taskId ? Number(filter.taskId) : undefined,
      type: filter.type || undefined
    })
    files.value = res.records
    pagination.total = res.total
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  filter.taskId = ''
  filter.type = ''
  loadFiles()
}

function handleFileChange(file: any) {
  selectedFile.value = file.raw
}

async function startUpload() {
  if (!selectedFile.value) return

  uploading.value = true
  uploadProgress.value = 0
  uploadStatusText.value = '计算文件MD5...'

  try {
    const file = selectedFile.value
    const fileMd5 = await computeFileMd5(file, (p) => {
      uploadProgress.value = Math.floor(p * 30) // MD5计算占30%进度
    })

    const totalChunks = Math.ceil(file.size / CHUNK_SIZE)

    // 初始化上传
    uploadStatusText.value = '初始化上传...'
    const initResult = await initUpload({
      fileMd5,
      fileName: file.name,
      fileSize: file.size,
      totalChunks,
      taskId: uploadForm.taskId,
      fileType: uploadForm.fileType
    })

    // 秒传检测
    if (!initResult.newFile) {
      uploadProgress.value = 100
      uploadStatusText.value = '文件已存在（秒传）'
      ElMessage.success('文件已存在，秒传成功')
      setTimeout(() => {
        showUploadDialog.value = false
        uploading.value = false
        loadFiles()
      }, 1500)
      return
    }

    // 断点续传：跳过已上传的分片
    const uploadedChunks = new Set(initResult.uploadedChunks || [])
    const chunksToUpload = totalChunks - uploadedChunks.size

    if (chunksToUpload < totalChunks) {
      uploadStatusText.value = `断点续传，已上传 ${uploadedChunks.size}/${totalChunks} 片`
    }

    // 上传分片
    let uploaded = uploadedChunks.size
    for (let i = 1; i <= totalChunks; i++) {
      if (uploadedChunks.has(i)) continue

      const start = (i - 1) * CHUNK_SIZE
      const end = Math.min(start + CHUNK_SIZE, file.size)
      const chunk = file.slice(start, end)

      const formData = new FormData()
      formData.append('chunkFile', chunk)
      formData.append('fileMd5', fileMd5)
      formData.append('chunkNumber', i.toString())

      await uploadChunk(formData)
      uploaded++
      uploadProgress.value = 30 + Math.floor((uploaded / totalChunks) * 60) // 分片上传占60%
      uploadStatusText.value = `上传分片 ${uploaded}/${totalChunks}`
    }

    // 合并分片
    uploadStatusText.value = '合并文件...'
    uploadProgress.value = 95
    await mergeChunks({
      fileMd5,
      surface: uploadForm.surface,
      keyPoint: uploadForm.keyPoint
    })

    uploadProgress.value = 100
    uploadStatusText.value = '上传完成'
    ElMessage.success('文件上传成功')
    setTimeout(() => {
      showUploadDialog.value = false
      uploading.value = false
      loadFiles()
    }, 1500)
  } catch (error: any) {
    ElMessage.error('上传失败: ' + (error.message || '未知错误'))
    uploading.value = false
  }
}

async function computeFileMd5(file: File, onProgress: (p: number) => void): Promise<string> {
  return new Promise((resolve, reject) => {
    const spark = new SparkMD5.ArrayBuffer()
    const reader = new FileReader()
    let current = 0

    reader.onload = (e) => {
      spark.append(e.target?.result as ArrayBuffer)
      current += CHUNK_SIZE
      onProgress(current / file.size)
      if (current < file.size) {
        readNext()
      } else {
        resolve(spark.end())
      }
    }

    reader.onerror = reject

    function readNext() {
      const slice = file.slice(current, Math.min(current + CHUNK_SIZE, file.size))
      reader.readAsArrayBuffer(slice)
    }

    readNext()
  })
}

async function handlePreview(row: MediaFile) {
  try {
    const url = await getPreviewUrl(row.id)
    previewUrl.value = url
    previewType.value = row.type
    showPreviewDialog.value = true
  } catch {
    ElMessage.error('获取预览链接失败')
  }
}

async function handleDownload(row: MediaFile) {
  try {
    const url = await getDownloadUrl(row.id)
    window.open(url, '_blank')
  } catch {
    ElMessage.error('获取下载链接失败')
  }
}

async function handleDelete(row: MediaFile) {
  await ElMessageBox.confirm(`确定删除文件 "${row.originalName}"？`, '提示', { type: 'warning' })
  await deleteMediaFile(row.id)
  ElMessage.success('删除成功')
  loadFiles()
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / 1024 / 1024).toFixed(1) + ' MB'
  return (bytes / 1024 / 1024 / 1024).toFixed(2) + ' GB'
}

function formatTime(time: string): string {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.filter-form {
  margin-bottom: 16px;
}
.upload-progress {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}
.progress-text {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
  text-align: center;
}
.preview-container {
  text-align: center;
}
</style>
