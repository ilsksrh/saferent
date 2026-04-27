# ai_service/main.py
from fastapi import FastAPI
from pydantic import BaseModel
import cv2
import numpy as np
import urllib.request
from skimage.metrics import structural_similarity as ssim

app = FastAPI(title="SafeRent AI Service")

class CompareRequest(BaseModel):
    beforeUrl: str
    afterUrl: str

class CompareResponse(BaseModel):
    ssimScore: float
    damageRegionCount: int
    damageDescription: str

def load_image_from_url(url: str):
    """Загрузить изображение по URL"""
    try:
        resp = urllib.request.urlopen(url)
        img_array = np.asarray(bytearray(resp.read()), dtype=np.uint8)
        return cv2.imdecode(img_array, cv2.IMREAD_COLOR)
    except Exception:
        return None

@app.post("/compare", response_model=CompareResponse)
def compare_photos(request: CompareRequest):
    img_before = load_image_from_url(request.beforeUrl)
    img_after  = load_image_from_url(request.afterUrl)

    # Если не удалось загрузить — нет ущерба (безопасный дефолт)
    if img_before is None or img_after is None:
        return CompareResponse(
            ssimScore=0.95,
            damageRegionCount=0,
            damageDescription="Could not load images — defaulting to no damage"
        )

    # Приводим к одному размеру
    h, w = img_before.shape[:2]
    img_after = cv2.resize(img_after, (w, h))

    # Конвертируем в grayscale
    gray_before = cv2.cvtColor(img_before, cv2.COLOR_BGR2GRAY)
    gray_after  = cv2.cvtColor(img_after,  cv2.COLOR_BGR2GRAY)

    # SSIM
    score, diff_map = ssim(gray_before, gray_after, full=True)
    diff_u8 = (diff_map * 255).astype("uint8")

    # Пороговая обработка
    _, thresh = cv2.threshold(
        diff_u8, 0, 255,
        cv2.THRESH_BINARY_INV | cv2.THRESH_OTSU
    )

    # Контуры повреждений
    contours, _ = cv2.findContours(
        thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE
    )
    damage_regions = [
        c for c in contours if cv2.contourArea(c) > 50
    ]

    # Описание
    if score >= 0.92:
        description = "No significant damage detected"
    elif score >= 0.70:
        description = f"Minor changes detected in {len(damage_regions)} areas"
    else:
        description = f"Significant damage in {len(damage_regions)} areas"

    return CompareResponse(
        ssimScore=round(float(score), 4),
        damageRegionCount=len(damage_regions),
        damageDescription=description
    )

@app.get("/health")
def health():
    return {"status": "ok"}