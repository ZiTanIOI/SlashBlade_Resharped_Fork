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
package jp.nyatla.nymmd;

import com.google.common.collect.Maps;
import jp.nyatla.nymmd.core.PmdBone;
import jp.nyatla.nymmd.core.PmdFace;
import jp.nyatla.nymmd.core.PmdIK;
import jp.nyatla.nymmd.struct.DataReader;
import jp.nyatla.nymmd.struct.pmd.*;
import jp.nyatla.nymmd.types.MmdTexUV;
import jp.nyatla.nymmd.types.MmdVector3;
import jp.nyatla.nymmd.types.PmdMaterial;
import jp.nyatla.nymmd.types.PmdSkinInfo;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

class DataComparator implements Comparator<PmdIK> {
    public int compare(PmdIK o1, PmdIK o2) {
        return (int) (o1.getSortVal() - o2.getSortVal());
    }
}

/**
 * PmdModelデータの格納クラス。PmdModelに関わるデータを提供します�?抽象関数 getResourceProviderを実装してください�?
 */
public abstract class MmdPmdModel_BasicClass {
    private String _name; // モデル名
    private int _number_of_vertex; // 頂点�?

    private PmdFace[] m_pFaceArray; // 表情配列
    private PmdBone[] m_pBoneArray; // ボーン配�?
    private Map<String, PmdBone> boneMap = Maps.newHashMap();
    private PmdIK[] m_pIKArray; // IK配列

    private MmdVector3[] _position_array; // 座標配列
    private MmdVector3[] _normal_array; // 法線配列
    private MmdTexUV[] _texture_uv; // テクスチャ座標配�?
    private PmdSkinInfo[] _skin_info_array;
    private PmdMaterial[] _materials; // マテリアル配�?
    private IResourceProvider _res_provider;

    public interface IResourceProvider {
        public ResourceLocation getTextureStream(String i_name) throws MmdException;
    }

    public MmdPmdModel_BasicClass(InputStream i_stream, IResourceProvider i_provider) throws MmdException {
        initialize(i_stream);
        this._res_provider = i_provider;
        return;
    }

    public int getNumberOfVertex() {
        return this._number_of_vertex;
    }

    public PmdMaterial[] getMaterials() {
        return this._materials;
    }

    public MmdTexUV[] getUvArray() {
        return this._texture_uv;
    }

    public MmdVector3[] getPositionArray() {
        return this._position_array;
    }

    public MmdVector3[] getNormatArray() {
        return this._normal_array;
    }

    public PmdSkinInfo[] getSkinInfoArray() {
        return this._skin_info_array;
    }

    public PmdFace[] getFaceArray() {
        return this.m_pFaceArray;
    }

    public PmdBone[] getBoneArray() {
        return this.m_pBoneArray;
    }

    public PmdIK[] getIKArray() {
        return this.m_pIKArray;
    }

    public PmdBone getBoneByName(String i_name) {
        return boneMap.get(i_name);
        /*
         * final PmdBone[] bone_array=this.m_pBoneArray; for(int i = 0 ; i <
         * bone_array.length ; i++) { final PmdBone bone=bone_array[i];
         * if(bone.getName().equals(i_name)) return bone; } return null;
         */
    }

    public PmdFace getFaceByName(String i_name) {
        final PmdFace[] face_array = this.m_pFaceArray;
        for (int i = 0; i < face_array.length; i++) {
            final PmdFace face = face_array[i];
            if (face.getName().equals(i_name))
                return face;
        }
        return null;
    }

    private void initialize(InputStream i_stream) throws MmdException {
        DataReader reader = new DataReader(i_stream);
        PMD_Header pPMDHeader = new PMD_Header();
        pPMDHeader.read(reader);
        if (!pPMDHeader.szMagic.equalsIgnoreCase("PMD")) {
            throw new MmdException();
        }

        this._name = pPMDHeader.szName;

        // -----------------------------------------------------
        // 頂点数取�?
        this._number_of_vertex = reader.readInt();//
        if (this._number_of_vertex < 0) {
            throw new MmdException();
        }

        // 頂点配列をコピー
        this._position_array = MmdVector3.createArray(this._number_of_vertex);
        this._normal_array = MmdVector3.createArray(this._number_of_vertex);
        this._texture_uv = MmdTexUV.createArray(this._number_of_vertex);
        this._skin_info_array = new PmdSkinInfo[this._number_of_vertex];

        PMD_Vertex tmp_pmd_vertex = new PMD_Vertex();
        for (int i = 0; i < _number_of_vertex; i++) {
            tmp_pmd_vertex.read(reader);
            _position_array[i].setValue(tmp_pmd_vertex.vec3Pos);
            _normal_array[i].setValue(tmp_pmd_vertex.vec3Normal);
            _texture_uv[i].setValue(tmp_pmd_vertex.uvTex);

            this._skin_info_array[i] = new PmdSkinInfo();
            this._skin_info_array[i].fWeight = tmp_pmd_vertex.cbWeight / 100.0f;
            this._skin_info_array[i].unBoneNo_0 = tmp_pmd_vertex.unBoneNo[0];
            this._skin_info_array[i].unBoneNo_1 = tmp_pmd_vertex.unBoneNo[1];
        }
        // -----------------------------------------------------
        // 頂点インデックス数取�?
        short[] indices_array = createIndicesArray(reader);

        // -----------------------------------------------------
        // マテリアル数取得
        int number_of_materials = reader.readInt();

        // マテリアル配列をコピ�?
        this._materials = new PmdMaterial[number_of_materials];

        PMD_Material tmp_pmd_material = new PMD_Material();

        int indices_ptr = 0;
        for (int i = 0; i < number_of_materials; i++) {
            tmp_pmd_material.read(reader);
            PmdMaterial pmdm = new PmdMaterial();
            pmdm.unknown = tmp_pmd_material.unknown;
            final int num_of_indices = tmp_pmd_material.ulNumIndices;

            pmdm.indices = new short[num_of_indices];
            System.arraycopy(indices_array, indices_ptr, pmdm.indices, 0, num_of_indices);
            indices_ptr += num_of_indices;

            pmdm.col4Diffuse.setValue(tmp_pmd_material.col4Diffuse);

            pmdm.col4Specular.r = tmp_pmd_material.col3Specular.r;
            pmdm.col4Specular.g = tmp_pmd_material.col3Specular.g;
            pmdm.col4Specular.b = tmp_pmd_material.col3Specular.b;
            pmdm.col4Specular.a = 1.0f;

            pmdm.col4Ambient.r = tmp_pmd_material.col3Ambient.r;
            pmdm.col4Ambient.g = tmp_pmd_material.col3Ambient.g;
            pmdm.col4Ambient.b = tmp_pmd_material.col3Ambient.b;
            pmdm.col4Ambient.a = 1.0f;

            pmdm.fShininess = tmp_pmd_material.fShininess;

            pmdm.texture_name = tmp_pmd_material.szTextureFileName;
            if (pmdm.texture_name.length() < 1) {
                pmdm.texture_name = null;
            }
            this._materials[i] = pmdm;

        }

        // Boneの読み出�?
        this.m_pBoneArray = createBoneArray(reader);
        boneMap.clear();
        Stream.of(this.m_pBoneArray).forEach(bone -> {
            this.boneMap.put(bone.getName(), bone);
        });
        // IK配列の読み出�?
        this.m_pIKArray = createIKArray(reader, this.m_pBoneArray);
        // Face配列の読み出�?
        this.m_pFaceArray = createFaceArray(reader);

        final PmdFace[] face_array = this.m_pFaceArray;
        if (face_array != null && 0 < face_array.length) {
            face_array[0].setFace(this._position_array);
        }
        return;
    }

    private static short[] createIndicesArray(DataReader i_reader) throws MmdException {
        int num_of_indeces = i_reader.readInt();
        short[] result = new short[num_of_indeces];
        result = new short[num_of_indeces];

        // 頂点インデックス配列をコピー
        for (int i = 0; i < num_of_indeces; i++) {
            result[i] = i_reader.readShort();
        }
        return result;
    }

    private static PmdBone[] createBoneArray(DataReader i_reader) throws MmdException {
        final int number_of_bone = i_reader.readShort();
        PMD_Bone tmp_pmd_bone = new PMD_Bone();

        PmdBone[] result = new PmdBone[number_of_bone];
        for (int i = 0; i < number_of_bone; i++) {
            tmp_pmd_bone.read(i_reader);
            // ボーンの親子関係を一緒に読みだすので�?
            result[i] = new PmdBone(tmp_pmd_bone, result);
        }
        for (int i = 0; i < number_of_bone; i++) {
            result[i].recalcOffset();
        }
        return result;
    }

    private static PmdIK[] createIKArray(DataReader i_reader, PmdBone[] i_ref_bone_array) throws MmdException {
        final int number_of_ik = i_reader.readShort();
        PMD_IK tmp_pmd_ik = new PMD_IK();
        PmdIK[] result = new PmdIK[number_of_ik];
        // IK配列を作�?
        if (number_of_ik > 0) {

            for (int i = 0; i < number_of_ik; i++) {
                tmp_pmd_ik.read(i_reader);
                result[i] = new PmdIK(tmp_pmd_ik, i_ref_bone_array);
            }
            Arrays.sort(result, new DataComparator());
        }
        return result;
    }

    private static PmdFace[] createFaceArray(DataReader i_reader) throws MmdException {
        final int number_of_face = i_reader.readShort();
        PMD_FACE tmp_pmd_face = new PMD_FACE();
        PmdFace[] result = new PmdFace[number_of_face];

        // 表情配列を作�?
        if (number_of_face > 0) {

            for (int i = 0; i < number_of_face; i++) {
                tmp_pmd_face.read(i_reader);
                result[i] = new PmdFace(tmp_pmd_face, result[0]);
            }
        }
        return result;
    }

    public String getModelName() {
        return this._name;
    }

    public IResourceProvider getResourceProvider() {
        return this._res_provider;
    }

}






