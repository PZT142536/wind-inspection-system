package com.shanghaiwind.media.service;

import org.springframework.stereotype.Service;

/**
 * 文件命名服务
 * 负责根据需求文档的命名规则生成文件夹和文件名
 */
@Service
public class FileNamingService {

    /**
     * 生成项目文件夹路径
     * 格式: {项目名称}/{机位号}/{检查类型}
     */
    public String generateProjectFolder(String projectName, String turbineCode, String inspectionType) {
        return String.format("%s/%s/%s", projectName, turbineCode, inspectionType);
    }

    /**
     * 生成叶片到货检查视频文件名
     * 格式: {叶片编号}-{检查面}-叶片检查记录视频
     */
    public String generateBladeArrivalVideoName(String bladeNo, String surface) {
        String surfaceName = getSurfaceName(surface);
        return String.format("%s-%s-叶片检查记录视频", bladeNo, surfaceName);
    }

    /**
     * 生成叶片到货检查照片文件名
     * 格式: {叶片编号}-{关键点名称}-叶片检查记录照片
     */
    public String generateBladeArrivalPhotoName(String bladeNo, String keyPoint) {
        return String.format("%s-%s-叶片检查记录照片", bladeNo, keyPoint);
    }

    /**
     * 生成轮毂到货检查视频文件名
     */
    public String generateHubArrivalVideoName() {
        return "轮毂（拆封前）检查记录视频";
    }

    /**
     * 生成机舱到货检查视频文件名
     */
    public String generateNacelleArrivalVideoName() {
        return "机舱（拆封前）检查记录视频";
    }

    /**
     * 生成塔筒到货检查视频文件名
     * 格式: 第{i}塔筒段检查记录视频
     */
    public String generateTowerArrivalVideoName(int sectionIndex) {
        return String.format("第%d塔筒段检查记录视频", sectionIndex);
    }

    /**
     * 生成箱变到货检查视频文件名
     */
    public String generateBoxTransformerArrivalVideoName() {
        return "箱变（拆封前）到货检查";
    }

    /**
     * 生成叶片吊装过程检查视频文件名
     * 格式: {叶片编号}叶片吊装过程检查-{序号}
     */
    public String generateBladeInstallVideoName(String bladeNo, int sequence) {
        return String.format("%s叶片吊装过程检查-%d", bladeNo, sequence);
    }

    /**
     * 生成风轮吊装过程检查视频文件名
     */
    public String generateRotorInstallVideoName() {
        return "风轮吊装过程检查";
    }

    /**
     * 生成机舱吊装过程检查视频文件名
     */
    public String generateNacelleInstallVideoName() {
        return "机舱吊装过程检查";
    }

    /**
     * 生成轮毂吊装过程检查视频文件名
     */
    public String generateHubInstallVideoName() {
        return "轮毂吊装过程检查";
    }

    /**
     * 生成塔筒吊装过程检查视频文件名
     * 格式: 塔筒吊装过程检查-{序号}
     */
    public String generateTowerInstallVideoName(int sequence) {
        return String.format("塔筒吊装过程检查-%d", sequence);
    }

    /**
     * 生成验收检查记录视频文件名
     */
    public String generateAcceptanceVideoName() {
        return "验收检查记录";
    }

    /**
     * 生成检查报告文件名
     * 格式: {项目名称}-{机位号}-{检查类型}-检查报告-{时间}
     */
    public String generateReportFileName(String projectName, String turbineCode, String inspectionType, String timestamp) {
        return String.format("%s-%s-%s-检查报告-%s", projectName, turbineCode, inspectionType, timestamp);
    }

    /**
     * 生成AI预警图片文件名
     * 格式: {风险ID}-{项目名称}-{机位号}-{时间}
     */
    public String generateAlertImageName(String riskId, String projectName, String turbineCode, String timestamp) {
        return String.format("%s-%s-%s-%s", riskId, projectName, turbineCode, timestamp);
    }

    /**
     * 获取检查面中文名称
     */
    private String getSurfaceName(String surface) {
        switch (surface) {
            case "leading_edge": return "前缘";
            case "trailing_edge": return "后缘";
            case "spar_cap": return "主梁面";
            default: return surface;
        }
    }

    /**
     * 获取检查类型中文名称
     */
    public String getInspectionTypeName(String type) {
        switch (type) {
            case "ARRIVAL": return "到货检查";
            case "INSTALL": return "施工监督";
            case "ACCEPTANCE": return "整体验收";
            default: return type;
        }
    }

    /**
     * 获取部套中文名称
     */
    public String getComponentName(String component) {
        switch (component) {
            case "BLADE": return "叶片到货检查";
            case "HUB": return "轮毂（拆封前）到货检查";
            case "NACELLE": return "机舱（拆封前）到货检查";
            case "TOWER": return "塔筒段到货检查";
            case "BOX_TRANSFORMER": return "箱变到货检查";
            default: return component;
        }
    }
}
