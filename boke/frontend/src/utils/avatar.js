/**
 * 修复头像URL路径
 * @param {string} avatarPath - 数据库中的头像路径
 * @returns {string} 修复后的完整URL
 */
export const fixAvatarUrl = (avatarPath) => {
    if (!avatarPath || avatarPath.trim() === '') {
        return '/uploads/images/default/touxiang.jpg'
    }

    let path = avatarPath

    // 如果已经是完整URL，直接返回
    if (path.startsWith('http')) {
        return path
    }

    // 修复数据库中的双斜杠路径
    if (path.startsWith('//')) {
        path = path.substring(1)
    }

    // 确保路径以 /uploads/ 开头
    if (!path.startsWith('/uploads/')) {
        if (path.startsWith('images/')) {
            path = '/uploads/' + path
        } else if (path.startsWith('/images/')) {
            path = '/uploads' + path
        } else {
            path = '/uploads/images/' + (path.startsWith('/') ? path.substring(1) : path)
        }
    }

    console.log('🖼️ 修复后的头像路径:', path)
    return path
}

/**
 * 处理图片加载失败的情况
 * @param {Event} event - 图片加载错误事件
 */
export const handleAvatarError = (event) => {
    console.error('❌ 图片加载失败:', event.target.src)
    // 使用绝对路径确保能正确加载默认头像
    event.target.src = '/uploads/images/default/touxiang.jpg'

    // 防止无限循环
    event.target.onerror = null
}