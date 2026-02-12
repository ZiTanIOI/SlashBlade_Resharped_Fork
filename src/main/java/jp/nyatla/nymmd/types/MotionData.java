/* 
 * PROJECT: NyMmd
 * --------------------------------------------------------------------------------
 * The MMD for Java is Java version MMD Motion player class library.
 * NyMmd is modules which removed the ARToolKit origin codes from ARTK_MMD,
 * and was ported to Java. 
 *
 * This is based on the ARTK_MMD v0.1 by PY.
 * http://ppyy.if.land.to/artk_mmd.html
 * py1024<at>gmail.com
 * http://www.nicovideo.jp/watch/sm7398691
 *
 * 
 * The MIT License
 * Copyright (C)2008-2012 nyatla
 * nyatla39<at>gmail.com
 * http://nyatla.jp
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
package jp.nyatla.nymmd.types;

import jp.nyatla.nymmd.core.PmdBone;

public class MotionData {
    public String szBoneName; // ボーン名
    public int ulNumKeyFrames; // キーフレーム�?
    public BoneKeyFrame[] pKeyFrames; // キーフレームデータ配�?

    /**
     * 
     * @param fFrame
     * @param i_pmd_bone 出力先オブジェク�?
     */
    public void getMotionPosRot(float fFrame, PmdBone i_pmd_bone) {
        int ulNumKeyFrame = this.ulNumKeyFrames;
        BoneKeyFrame[] bone_key_frame = this.pKeyFrames;

        // 最終フレームを過ぎていた場�?
        if (fFrame > bone_key_frame[ulNumKeyFrame - 1].fFrameNo) {
            fFrame = bone_key_frame[ulNumKeyFrame - 1].fFrameNo;
        }

        // 現在の時間がどのキー近辺にあるか
        int lKey0 = findByBinarySearch(bone_key_frame, fFrame, 0, ulNumKeyFrame - 1) - 1;
        int lKey1 = lKey0 + 1;
        if (lKey1 == ulNumKeyFrame) {
            lKey1 = ulNumKeyFrame - 1;
        }
        if (lKey0 < 0) {
            lKey0 = 0;
        }
        // 前後のキーの時間
        float fTime0 = bone_key_frame[lKey0].fFrameNo;
        float fTime1 = bone_key_frame[lKey1].fFrameNo;

        MmdVector3 pvec3Pos = i_pmd_bone.m_vec3Position;
        MmdVector4 pvec4Rot = i_pmd_bone.m_vec4Rotate;

        // 前後のキーの間でどの位置にいるか
        if (lKey0 != lKey1) {
            float fLerpValue = (fFrame - fTime0) / (fTime1 - fTime0);
            pvec3Pos.Vector3Lerp(bone_key_frame[lKey0].vec3Position, bone_key_frame[lKey1].vec3Position, fLerpValue);
            pvec4Rot.QuaternionSlerp(bone_key_frame[lKey0].vec4Rotate, bone_key_frame[lKey1].vec4Rotate, fLerpValue);
            pvec4Rot.QuaternionNormalize(pvec4Rot);// これほんとにいるの？
        } else {
            pvec3Pos.setValue(bone_key_frame[lKey0].vec3Position);
            pvec4Rot.setValue(bone_key_frame[lKey0].vec4Rotate);
        }
    }

    /**
     * @author やねうら�?さん
     * @param pKeyFrames
     * @param fFrame
     * @param start
     * @param end
     * @return
     */
    private static int findByBinarySearch(BoneKeyFrame[] pKeyFrames, float fFrame, int start, int end) {
        int diff = end - start;
        if (diff < 8) {
            // ある程度小さくなったら逐次サーチ。このな かに見つかるはずなんだ�?
            for (int i = start; i < end; i++) {
                if (fFrame < pKeyFrames[i].fFrameNo) {
                    return i;
                }
            }
            return end;
        }

        // 再帰的に調べ�?
        int mid = (start + end) / 2;
        if (fFrame < pKeyFrames[mid].fFrameNo) {
            return findByBinarySearch(pKeyFrames, fFrame, start, mid);
        } else {
            return findByBinarySearch(pKeyFrames, fFrame, mid, end);
        }
    }
}






