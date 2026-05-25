<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>风机施工安装监测与质量巡检系统</h2>
      </div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="empNo">
          <el-input
            v-model="loginForm.empNo"
            placeholder="请输入工号"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              prefix-icon="Key"
              size="large"
            />
            <img
              v-if="captchaImage"
              :src="captchaImage"
              class="captcha-img"
              @click="refreshCaptcha"
            />
            <div v-else class="captcha-img captcha-loading" @click="refreshCaptcha">
              点击获取
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getCaptcha } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')

const loginForm = ref({
  empNo: '',
  password: '',
  captcha: '',
})

const loginRules = {
  empNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

// 获取验证码
async function refreshCaptcha() {
  try {
    const res = await getCaptcha()
    captchaImage.value = 'data:image/gif;base64,' + res.image
    captchaKey.value = res.key
  } catch (error) {
    console.error('获取验证码失败', error)
  }
}

// 登录
async function handleLogin() {
  const valid = await loginFormRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({
      empNo: loginForm.value.empNo,
      password: loginForm.value.password,
      captcha: loginForm.value.captcha,
      captchaKey: captchaKey.value,
    })
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    refreshCaptcha()
    loginForm.value.captcha = ''
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;

  h2 {
    font-size: 20px;
    color: #333;
    font-weight: 600;
  }
}

.login-form {
  .captcha-row {
    display: flex;
    gap: 12px;
    width: 100%;
  }

  .captcha-img {
    height: 40px;
    cursor: pointer;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
  }

  .captcha-loading {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16px;
    font-size: 12px;
    color: #999;
  }

  .login-btn {
    width: 100%;
  }
}
</style>
