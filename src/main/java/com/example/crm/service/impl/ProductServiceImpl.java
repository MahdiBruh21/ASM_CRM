package com.example.crm.service.impl;

import com.example.crm.dto.ProductDTO;
import com.example.crm.model.Product;
import com.example.crm.repository.ProductRepository;
import com.example.crm.service.interfaces.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pgvector.PGvector;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ObjectMapper objectMapper;

    public ProductServiceImpl(ProductRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void initializeProducts() {
        if (repository.count() > 0) {
            return; // Avoid duplicating data
        }

        // Product data from document
        ProductDTO[] initialProducts = new ProductDTO[] {
                createProductDTO("DUX ERP", createDescription(
                        "Modular architecture with integration across accounting, CRM, stock, sales, and HR. Supports multi-company, multi-user, multi-language, multi-currency. Configurable document management (quotes, invoices, orders, etc.). OCR for invoice capture, AI-based automation. Web and Android-based mobile access via 'Dux Nomade'. Enterprises needing compliant ERP with Tunisian regulatory support.",
                        "Architecture modulaire (gestion commerciale, comptabilité, stock, CRM, RH). Multi-société, multi-utilisateur, multi-langue, multi-devise. Circuit documentaire complet (devis, bons de commande, factures, etc.). Capture OCR des factures, automatisation par IA. Accès web et mobile via l'application Android 'Dux Nomade'.",
                        "هيكلية معيارية تشمل المحاسبة، المبيعات، المخزون، علاقات العملاء، والموارد البشرية. دعم لعدة شركات ومستخدمين ولغات وعملات. إدارة الدورة الوثائقية (عروض أسعار، أوامر، فواتير). التعرف الضوئي على الفواتير (OCR) ومعالجة ذكية بالذكاء الاصطناعي. واجهة ويب وتطبيق موبايل 'Dux Nomade' لأندرويد.",
                        "نظام ERP كامل يشمل المحاسبة، المبيعات، الستوك، CRM، والموارد البشرية. يدعم برشا شركات ويوزارات ولغات وعملات. يخرج ديڤي وفواتير وأوامر. فيه OCR وذكاء اصطناعي للتعرف عالفواتير. يخدم عالواب وفيه أپليكاسيون 'Dux Nomade'.",
                        "DUX ERP: système ERP yekhdem 3la web w mobile, multi-langue, 7isab, CRM, stock, multi-société, multi-user, multi-devise. Andou OCR ta3rf 3al factures automatiquement. Ynafe3 les sociétés eli 7ajja b ERP tounsi."
                )),
                createProductDTO("Pro Déclaration Employeur", createDescription(
                        "Generation and submission of employer tax declarations as required in Tunisia. Supports annexes I, II, IV, V, VI, VII. Generates withholding reports (salary, pension, rent, commissions). Four main menu sections: parameters, beneficiaries, declaration management, annexes. Exports in PDF/TXT formats. Fully aligned with Tunisian tax legislation.",
                        "Génération des déclarations mensuelles de retenue à la source employeur en Tunisie. Génération des annexes I, II, IV, V, VI, VII. Gestion des retenues sur salaires, pensions, loyers, honoraires, etc. Interface organisée en 4 menus : paramètres, bénéficiaires, gestion des déclarations, annexes. Export PDF et TXT. Conforme à la législation fiscale tunisienne.",
                        "توليد التصاريح الشهرية بالاقتطاعات من المصدر للمشغلين وفق القوانين التونسية. دعم للملاحق I، II، IV، V، VI، VII. إدارة الاقتطاعات (رواتب، معاشات، كراء، خدمات). واجهة تحتوي على: الإعدادات، المستفيدين، إدارة التصاريح، الملاحق. تصدير الملفات بصيغ PDF وTXT. مطابق للتشريع الجبائي التونسي.",
                        "تصريح شهري بالاقتطاعات للمشغلين حسب القانون التونسي. يخرج الملاحق I، II، IV، V، VI، VII. يتعامل مع الرواتب، الكراء، الخدمات. فيه 4 أقسام: إعدادات، مستفيدين، إدارة، ملاحق. يصدّر PDF وTXT. مطابق للقانون.",
                        "Pro Déclaration Employeur: barmajia ta3 déclarations fiscales, tajhez annexes I, II, IV, V, VI, VII, te5ou data ta3 salaires, loyers, services. Interface 4 sections: param, bénéficiaires, gestion, annexes. Export en PDF w TXT."
                )),
                createProductDTO("Pro Déclaration CNSS", createDescription(
                        "Generates monthly CNSS (Caisse Nationale de Sécurité Sociale) submissions. Automates contribution calculation. Prepares CNSS submission files in accepted format. Designed for use by HR departments and accounting firms. Compliant with Tunisian CNSS rules.",
                        "Génération des fichiers de déclaration CNSS mensuelle (Caisse Nationale de Sécurité Sociale – Tunisie). Calcul automatique des cotisations sociales. Préparation des fichiers à soumettre à la CNSS. Compatible avec Pro Paie pour la gestion de la paie. Conforme aux obligations de déclaration CNSS en Tunisie.",
                        "إعداد التصاريح الشهرية لمساهمات CNSS (الصندوق الوطني للضمان الاجتماعي – تونس). احتساب المساهمات تلقائيًا. توليد ملفات التصريح بصيغة مقبولة لدى CNSS. تكامل مباشر مع Pro Paie (إدارة الرواتب). متوافق مع متطلبات CNSS.",
                        "تصريح شهري بالصندوق الوطني للضمان الاجتماعي. يحسب الاشتراكات آليًا. يخرج دوسيات جاهزة للإرسال. يتكامل مع Pro Paie. يناسب HR والمحاسبين.",
                        "Pro Déclaration CNSS: barmajia ta3 tasri7 bel CNSS, calcul automatique ta3 cotisations, tajhez el fichier eli tetb3ath lil CNSS, tekhdem m3a Pro Paie, ynafe3 HR w comptables."
                )),
                createProductDTO("Pro Paie", createDescription(
                        "Payroll management software for employee records and payslips. Employee records (contracts, salary, leave, loans). Payslip generation and archiving. Report generation (Excel, PDF, TXT). Multi-company and multi-user support. Export options compatible with tax and CNSS modules. Seamless with DUX ERP and declaration modules.",
                        "Gestion des employés : contrats, salaires, congés, prêts. Génération de bulletins de paie. Édition de rapports en formats Excel, PDF, TXT. Multi-société, multi-utilisateur. Interopérabilité avec DUX ERP et les modules de déclaration.",
                        "إدارة عقود الموظفين، الرواتب، القروض، الغيابات. إنشاء وطباعة كشوف الرواتب. تقارير بأشكال متعددة (Excel، PDF، TXT). دعم لعدة شركات ومستخدمين. متكامل مع DUX ERP وبرامج التصريح الأخرى.",
                        "برنامج لإدارة الرواتب والموظفين. يخزن معلومات العقود، الغيابات، القروض، السالير. يخرج شهريات. يطبع تقارير Excel، PDF، TXT. يخدم في برشا شركات بنفس الوقت. يتكامل مع DUX ERP.",
                        "Pro Paie: gestion ta3 salaires, données ta3 el employés: 3ou9oud, sallaire, conges, qroudh. Bulletin de paie w reporting. Yexporti PDF, Excel, TXT. Multi-user, multi-entreprise. Tekhdem m3a DUX ERP w déclaration modules."
                )),
                createProductDTO("Pro Déclaration Achat en Suspension", createDescription(
                        "Manages tax declarations for purchases made under VAT suspension rules. Data entry of suspended purchases. Report generation for tax authorities. Often used with the 'Ventes en Suspension' counterpart. Target audience: import/export businesses and VAT-exempt organizations.",
                        "Gestion des achats effectués sous régime suspensif de TVA. Saisie et suivi des factures d’achats en suspension. Édition de relevés à transmettre à l’administration fiscale. Généralement utilisé avec le module 'Déclaration Vente en Suspension'.",
                        "إدارة المشتريات التي تتم وفق نظام التعليق الضريبي (TVA). إدخال وتتبع فواتير الشراء المعفاة. إعداد تقارير للإدارة الجبائية. يستخدم غالبًا مع برنامج 'تصريح البيع تحت التعليق'.",
                        "تسجيل المشتريات اللي ما فيهمش TVA. تخرج تقارير للقباضة. تنجم تستعمل مع البيع تحت التعليق. تنفع للشركات اللي تصدر.",
                        "Pro Déclaration Achat en Suspension: système déclaration bel TVA suspension, t7ot fatura mchet b’ suspension, t5arrej déclaration lil 9abdha, tekhdem m3a 'Vente en Suspension'."
                )),
                createProductDTO("Procheque", createDescription(
                        "Customized printing of cheques and promissory notes (traites). Compatible with most Tunisian banks and standard check formats. Stores payee data and history. Target use: SMEs, accounting offices, finance teams. Part of Comptabilité suite.",
                        "Impression personnalisée des chèques bancaires et des traites. Modèles compatibles avec les banques tunisiennes. Saisie rapide des bénéficiaires. Historique des paiements et automatisation. Intégré dans la suite Comptabilité d’ASM.",
                        "طباعة الشيكات والكمبيالات بصيغ قانونية حسب المعايير البنكية التونسية. دعم لنماذج البنوك التونسية المختلفة. تخزين بيانات المستفيدين. أرشفة وتتبع السجلات.",
                        "طباعة شيكات وكمبيالات حسب نماذج البنوك التونسية. تخزن معلومات المستفيدين. أرشيف للمدفوعات. تنفع للمحاسبين والشركات.",
                        "Procheque: tba3a ta3 chèque w traites, modèles ta3 bnouk tounsiya, t5alli info ta3 bénéficiaires, archive ta3 chèkèt."
                )),
                createProductDTO("ProResto", createDescription(
                        "POS software for restaurants and cafés. Order taking, table management, receipts. Waiter performance tracking. Recipe/cost tracking. Android-based mobile app (ProResto Mobile). Integrates with POS hardware (touchscreens, receipt printers). Designed for restaurants, cafés, fast food, pizzerias, tea salons.",
                        "Prise de commande, gestion des tables, impression des tickets. Suivi des performances des serveurs. Gestion des fiches techniques et des coûts. Application mobile Android (ProResto Mobile). Compatible avec les matériels POS d’ASM (caisse tactile, imprimante ticket).",
                        "إدارة نقاط البيع – قطاع المطاعم. استقبال الطلبات، إدارة الطاولات، طباعة الفواتير. تتبع أداء النُدُل. إدارة وصفات الأطباق وتكاليفها. تطبيق موبايل Android: ProResto Mobile. تكامل مع أجهزة نقاط البيع (POS) الخاصة بـ ASM.",
                        "برنامج للمطاعم والمقاهي. يسهل الطلبات، الطاولات، والفواتير. يتابع الڨارسونات. يحسب تكاليف الوصفات. فيه أپليكاسيون ProResto Mobile. يركب مع الكاشيات والطابعات.",
                        "ProResto: POS lil restauration, commande, gestion ta3 tawél, tikets, suivi performance ta3 les garçons, recettes, prix de revient, Android app ProResto Mobile, tekhdem m3a POS hardware."
                ))
        };

        for (ProductDTO productDTO : initialProducts) {
            saveProduct(productDTO);
        }
    }

    private ProductDTO createProductDTO(String name, String descriptionJson) {
        ProductDTO dto = new ProductDTO();
        dto.setDescription(descriptionJson);
        return dto;
    }

    private String createDescription(String english, String french, String arabic, String darija, String arabizi) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("english", english);
        node.put("french", french);
        node.put("arabic", arabic);
        node.put("darija", darija);
        node.put("arabizi", arabizi);
        return node.toString();
    }

    @Override
    public ProductDTO saveProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setDescription(productDTO.getDescription());
        float[] embedding = generateEmbedding(productDTO.getDescription());
        product.setRagVectorFromArray(embedding);
        try {
            product = repository.save(product);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save product: " + e.getMessage(), e);
        }
        return mapToDTO(product);
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        return mapToDTO(product);
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        return repository.findAll().stream().map(this::mapToDTO).toList();
    }

    @Override
    public ProductDTO findNearestProduct(float[] queryVector) {
        Product product = repository.findNearestNeighbor(new PGvector(queryVector))
                .orElseThrow(() -> new RuntimeException("No similar product found"));
        return mapToDTO(product);
    }

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setDescription(product.getDescription());
        try {
            dto.setRagVector(product.getRagVector() != null ? product.getRagVector().toArray() : null);
        } catch (Exception e) {
            System.err.println("Error converting PGvector to array: " + e.getMessage());
            dto.setRagVector(null);
        }
        return dto;
    }

    private float[] generateEmbedding(String text) {
        // Placeholder: Replace with actual embedding service (e.g., multilingual-e5-large)
        return new float[384]; // Dummy embedding
    }
}