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
package jp.nyatla.nymmd.struct.pmd;

import jp.nyatla.nymmd.MmdException;
import jp.nyatla.nymmd.struct.DataReader;
import jp.nyatla.nymmd.struct.StructReader;
import jp.nyatla.nymmd.struct.StructType;
import jp.nyatla.nymmd.types.MmdTexUV;
import jp.nyatla.nymmd.types.MmdVector3;

public class PMD_Vertex implements StructType {
    public MmdVector3 vec3Pos = new MmdVector3(); // 座標
    public MmdVector3 vec3Normal = new MmdVector3(); // 法線ベクトル
    public MmdTexUV uvTex = new MmdTexUV(); // テクスチャ座�?

    public int[] unBoneNo = new int[2]; // ボーン番�?
    public int cbWeight; // ブレンドの重�?(0�?00�?
    public int cbEdge; // エッジフラグ
    /*
     * Vector3 vec3Pos; // 座標 Vector3 vec3Normal; // 法線ベクトル TexUV uvTex; // テクスチャ座�?
     * 
     * unsigned short unBoneNo[2]; // ボーン番�?unsigned char cbWeight; // ブレンドの重�?
     * (0�?00�? unsigned char cbEdge; // エッジフラグ
     */

    public void read(DataReader i_reader) throws MmdException {
        StructReader.read(this.vec3Pos, i_reader);
        StructReader.read(this.vec3Normal, i_reader);
        StructReader.read(this.uvTex, i_reader);
        this.unBoneNo[0] = i_reader.readUnsignedShort();
        this.unBoneNo[1] = i_reader.readUnsignedShort();
        this.cbWeight = i_reader.read();
        this.cbEdge = i_reader.read();
        return;
    }

}






