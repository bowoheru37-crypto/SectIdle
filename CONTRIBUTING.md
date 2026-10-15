# 🤝 CONTRIBUTING TO SECT IDLE & CHIMERA SDK

Thank you for your interest in contributing to **Sect Idle** and the **Chimera Unified SDK v6.0**!

## 🛠️ How to Contribute

1. **Fork the Repository**: Create your own feature branch from `main`.
2. **Sketchware Pro & Low-Spec Compatibility**:
   - Keep Java code 100% compliant with **Java 7/8 standard syntax** (No lambda expressions, method references, or streams) to maintain 100% compatibility with **Sketchware Pro v7.0.0**.
   - Keep memory consumption minimal (<89MB peak RSS) for 1GB RAM low-entry devices (Itel A70).
3. **Test Your Changes**:
   - Run `./gradlew test`
   - Run `node build_v6.js --all`
   - Run `python3 auto_test.py`
4. **Submit a Pull Request**: Provide a detailed description of your changes and test results.

---

## 📜 Code Style Guidelines
- Pure Java in `app/` and `sketchware_pro_v7/`.
- Modular Python scripts in `src/`.
- Lightweight Node.js pipeline scripts in root.
